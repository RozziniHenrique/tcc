package com.tcc.uscs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.aluno.Aluno;
import com.tcc.uscs.model.cliente.Cliente;
import com.tcc.uscs.model.curso.Curso;
import com.tcc.uscs.model.me.dto.AdicionarPerfilAlunoDTO;
import com.tcc.uscs.model.me.dto.AdicionarPerfilClienteDTO;
import com.tcc.uscs.model.me.dto.AtualizarMeuPerfilDTO;
import com.tcc.uscs.model.usuario.TipoUsuario;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.AlunoRepository;
import com.tcc.uscs.repository.ClienteRepository;
import com.tcc.uscs.repository.CursoRepository;
import com.tcc.uscs.repository.UsuarioRepository;
import java.util.HashSet;
import java.util.Optional;
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

  @Mock
  private ClienteRepository clienteRepository;

  @Mock
  private AlunoRepository alunoRepository;

  @Mock
  private CursoRepository cursoRepository;

  @InjectMocks
  private MeuPerfilService service;

  @AfterEach
  void limparContexto() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void deveDetalharUsuarioAutenticado() {
    var usuario = autenticarUsuario(TipoUsuario.CLIENTE);

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
    var usuario = autenticarUsuario(TipoUsuario.CLIENTE);
    when(usuario.getId()).thenReturn(1L);
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

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
  }

  @Test
  void deveAdicionarPerfilCliente() {
    var usuario = autenticarUsuario(TipoUsuario.ALUNO);
    when(usuario.getId()).thenReturn(1L);
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

    var resultado = service.adicionarPerfilCliente(
      new AdicionarPerfilClienteDTO("Preferências do cliente")
    );

    assertTrue(resultado.perfis().contains("CLIENTE"));
    verify(clienteRepository).save(any(Cliente.class));
  }

  @Test
  void deveRecusarPerfilClienteDuplicado() {
    var usuario = autenticarUsuario(TipoUsuario.CLIENTE);
    when(usuario.getId()).thenReturn(1L);
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

    var erro = assertThrows(ValidacaoException.class, () ->
      service.adicionarPerfilCliente(new AdicionarPerfilClienteDTO(null))
    );

    assertEquals("O usuário já possui o perfil CLIENTE.", erro.getMessage());
    verifyNoInteractions(clienteRepository);
  }

  @Test
  void deveAdicionarPerfilAlunoComCursoAtivo() {
    var usuario = autenticarUsuario(TipoUsuario.CLIENTE);
    var curso = mock(Curso.class);

    when(usuario.getId()).thenReturn(1L);
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
    when(cursoRepository.findByIdAndAtivoTrue(10L)).thenReturn(
      Optional.of(curso)
    );

    var resultado = service.adicionarPerfilAluno(
      new AdicionarPerfilAlunoDTO(10L)
    );

    assertTrue(resultado.perfis().contains("ALUNO"));
    verify(alunoRepository).save(any(Aluno.class));
  }

  @Test
  void deveRecusarPerfilAlunoComCursoInativoOuInexistente() {
    var usuario = autenticarUsuario(TipoUsuario.CLIENTE);

    when(usuario.getId()).thenReturn(1L);
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
    when(cursoRepository.findByIdAndAtivoTrue(10L)).thenReturn(
      Optional.empty()
    );

    var erro = assertThrows(ValidacaoException.class, () ->
      service.adicionarPerfilAluno(new AdicionarPerfilAlunoDTO(10L))
    );

    assertEquals("Curso não encontrado ou inativo.", erro.getMessage());
    verifyNoInteractions(alunoRepository);
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

  private Usuario autenticarUsuario(TipoUsuario perfilInicial) {
    var usuario = mock(Usuario.class);
    var perfis = new HashSet<TipoUsuario>();
    perfis.add(perfilInicial);

    when(usuario.isEnabled()).thenReturn(true);
    when(usuario.getPerfis()).thenReturn(perfis);

    var authentication = new UsernamePasswordAuthenticationToken(
      usuario,
      null,
      Set.of()
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);

    return usuario;
  }
}
