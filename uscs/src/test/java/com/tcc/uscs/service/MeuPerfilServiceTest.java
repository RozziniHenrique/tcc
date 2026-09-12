package com.tcc.uscs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.me.dto.AtualizarMeuPerfilDTO;
import com.tcc.uscs.model.usuario.TipoUsuario;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.UsuarioRepository;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class MeuPerfilServiceTest {

  @Mock
  private UsuarioRepository usuarioRepository;

  @InjectMocks
  private MeuPerfilService service;

  @AfterEach
  void limparContexto() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void deveDetalharUsuarioAutenticado() {
    var usuario = autenticarUsuario();

    when(usuario.getId()).thenReturn(1L);
    when(usuario.getNome()).thenReturn("Henrique");
    when(usuario.getEmail()).thenReturn("henrique@email.com");

    var resultado = service.detalhar();

    assertEquals(1L, resultado.id());
    assertEquals("Henrique", resultado.nome());
    assertEquals("henrique@email.com", resultado.email());
    assertTrue(resultado.perfis().contains("CLIENTE"));
  }

  @Test
  void deveAtualizarSomenteDadosPermitidos() {
    var usuario = autenticarUsuario();
    var dados = new AtualizarMeuPerfilDTO(
      "Henrique Rossini",
      "11999999999",
      "São Paulo"
    );

    service.atualizar(dados);

    verify(usuario).atualizarInformacoes(
      "Henrique Rossini",
      null,
      "11999999999",
      "São Paulo"
    );
    verify(usuarioRepository).save(usuario);
  }

  @Test
  void deveRecusarQuandoNaoExisteAutenticacao() {
    SecurityContextHolder.clearContext();

    assertThrows(ValidacaoException.class, () -> service.detalhar());

    verifyNoInteractions(usuarioRepository);
  }

  @Test
  void deveRecusarUsuarioInativo() {
    var usuario = mock(Usuario.class);
    var authentication = new UsernamePasswordAuthenticationToken(
      usuario,
      null,
      Set.of()
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);
    when(usuario.isEnabled()).thenReturn(false);

    assertThrows(ValidacaoException.class, () -> service.detalhar());
  }

  private Usuario autenticarUsuario() {
    var usuario = mock(Usuario.class);

    when(usuario.isEnabled()).thenReturn(true);
    when(usuario.getPerfis()).thenReturn(Set.of(TipoUsuario.CLIENTE));

    var authentication = new UsernamePasswordAuthenticationToken(
      usuario,
      null,
      Set.of()
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);

    return usuario;
  }
}
