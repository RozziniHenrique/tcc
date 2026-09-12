package com.tcc.uscs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.tcc.uscs.infra.exception.TokenInvalidoException;
import com.tcc.uscs.model.usuario.RefreshToken;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.RefreshTokenRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

  @Mock
  private RefreshTokenRepository repository;

  @InjectMocks
  private RefreshTokenService service;

  @BeforeEach
  void configurar() {
    ReflectionTestUtils.setField(service, "expirationDays", 30L);
  }

  @Test
  void deveCriarRefreshTokenSeguro() {
    var usuario = mock(Usuario.class);

    var tokenGerado = service.criar(usuario);

    var captor = ArgumentCaptor.forClass(RefreshToken.class);
    verify(repository).save(captor.capture());

    var tokenSalvo = captor.getValue();

    assertNotNull(tokenGerado);
    assertFalse(tokenGerado.isBlank());
    assertNotEquals(tokenGerado, tokenSalvo.getTokenHash());
    assertEquals(64, tokenSalvo.getTokenHash().length());
    assertEquals(usuario, tokenSalvo.getUsuario());
    assertFalse(tokenSalvo.isRevogado());
    assertTrue(tokenSalvo.getDataExpiracao().isAfter(LocalDateTime.now()));
  }

  @Test
  void deveValidarRefreshTokenAtivo() {
    var usuario = mock(Usuario.class);
    when(usuario.isEnabled()).thenReturn(true);

    var tokenGerado = service.criar(usuario);

    var captor = ArgumentCaptor.forClass(RefreshToken.class);
    verify(repository).save(captor.capture());

    var tokenSalvo = captor.getValue();

    when(
      repository.findByTokenHashAndRevogadoFalse(tokenSalvo.getTokenHash())
    ).thenReturn(Optional.of(tokenSalvo));

    var resultado = service.validarEObterUsuario(tokenGerado);

    assertEquals(usuario, resultado);
  }

  @Test
  void deveRecusarTokenInexistente() {
    when(repository.findByTokenHashAndRevogadoFalse(anyString())).thenReturn(
      Optional.empty()
    );

    assertThrows(TokenInvalidoException.class, () ->
      service.validarEObterUsuario("token-inexistente")
    );
  }

  @Test
  void deveRecusarTokenExpirado() {
    var usuario = mock(Usuario.class);
    var token = new RefreshToken();

    token.setUsuario(usuario);
    token.setDataExpiracao(LocalDateTime.now().minusDays(1));
    token.setRevogado(false);

    when(repository.findByTokenHashAndRevogadoFalse(anyString())).thenReturn(
      Optional.of(token)
    );

    assertThrows(TokenInvalidoException.class, () ->
      service.validarEObterUsuario("token-expirado")
    );
  }

  @Test
  void deveRevogarRefreshToken() {
    var token = new RefreshToken();

    when(repository.findByTokenHashAndRevogadoFalse(anyString())).thenReturn(
      Optional.of(token)
    );

    service.revogar("refresh-token");

    assertTrue(token.isRevogado());
    verify(repository).save(token);
  }

  @Test
  void deveRevogarTodosOsTokensDoUsuario() {
    var usuario = mock(Usuario.class);

    service.revogarTodos(usuario);

    verify(repository).deleteAllByUsuario(usuario);
  }
}
