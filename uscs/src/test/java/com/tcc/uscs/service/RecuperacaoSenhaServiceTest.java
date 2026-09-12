package com.tcc.uscs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.usuario.PasswordResetToken;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.model.usuario.dto.RedefinirSenhaDTO;
import com.tcc.uscs.model.usuario.dto.SolicitarRecuperacaoSenhaDTO;
import com.tcc.uscs.model.usuario.dto.VerificarCodigoSenhaDTO;
import com.tcc.uscs.repository.PasswordResetTokenRepository;
import com.tcc.uscs.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RecuperacaoSenhaServiceTest {

  @Mock
  private UsuarioRepository usuarioRepository;

  @Mock
  private PasswordResetTokenRepository tokenRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private EmailService emailService;

  @Mock
  private RefreshTokenService refreshTokenService;

  @InjectMocks
  private RecuperacaoSenhaService service;

  @BeforeEach
  void configurar() {
    ReflectionTestUtils.setField(service, "expirationMinutes", 15L);
    ReflectionTestUtils.setField(service, "maxAttempts", 5);
  }

  @Test
  void naoDeveRevelarQuandoEmailNaoExiste() {
    var dados = new SolicitarRecuperacaoSenhaDTO("inexistente@email.com");

    when(usuarioRepository.findByEmailAndAtivoTrue(dados.email())).thenReturn(
      Optional.empty()
    );

    service.solicitarRecuperacao(dados);

    verifyNoInteractions(
      tokenRepository,
      passwordEncoder,
      emailService,
      refreshTokenService
    );
  }

  @Test
  void deveCriarCodigoHashEEnviarEmail() {
    var usuario = mock(Usuario.class);

    when(
      usuarioRepository.findByEmailAndAtivoTrue("usuario@email.com")
    ).thenReturn(Optional.of(usuario));
    when(usuario.getEmail()).thenReturn("usuario@email.com");
    when(passwordEncoder.encode(anyString())).thenReturn("codigo-hash");

    service.solicitarRecuperacao(
      new SolicitarRecuperacaoSenhaDTO("usuario@email.com")
    );

    var captor = ArgumentCaptor.forClass(PasswordResetToken.class);
    verify(tokenRepository).save(captor.capture());

    var token = captor.getValue();

    assertEquals("codigo-hash", token.getToken());
    assertEquals(usuario, token.getUsuario());
    assertEquals(0, token.getTentativas());
    assertTrue(token.getDataExpiracao().isAfter(LocalDateTime.now()));

    verify(tokenRepository).deleteByUsuario(usuario);
    verify(emailService).enviarCodigoRecuperacao(
      eq("usuario@email.com"),
      argThat(codigo -> codigo.matches("\\d{6}")),
      eq(15L)
    );
  }

  @Test
  void deveIncrementarTentativaQuandoCodigoForInvalido() {
    var token = tokenValido(0);

    when(tokenRepository.findByUsuarioEmail("usuario@email.com")).thenReturn(
      Optional.of(token)
    );
    when(passwordEncoder.matches("000000", "codigo-hash")).thenReturn(false);

    assertThrows(ValidacaoException.class, () ->
      service.verificarCodigo(
        new VerificarCodigoSenhaDTO("usuario@email.com", "000000")
      )
    );

    assertEquals(1, token.getTentativas());
    verify(tokenRepository).save(token);
  }

  @Test
  void deveExcluirTokenAoAtingirLimiteDeTentativas() {
    var token = tokenValido(4);

    when(tokenRepository.findByUsuarioEmail("usuario@email.com")).thenReturn(
      Optional.of(token)
    );
    when(passwordEncoder.matches("000000", "codigo-hash")).thenReturn(false);

    assertThrows(ValidacaoException.class, () ->
      service.verificarCodigo(
        new VerificarCodigoSenhaDTO("usuario@email.com", "000000")
      )
    );

    verify(tokenRepository).delete(token);
    verify(tokenRepository, never()).save(token);
  }

  @Test
  void deveExcluirTokenExpirado() {
    var token = tokenValido(0);
    token.setDataExpiracao(LocalDateTime.now().minusMinutes(1));

    when(tokenRepository.findByUsuarioEmail("usuario@email.com")).thenReturn(
      Optional.of(token)
    );

    assertThrows(ValidacaoException.class, () ->
      service.verificarCodigo(
        new VerificarCodigoSenhaDTO("usuario@email.com", "123456")
      )
    );

    verify(tokenRepository).delete(token);
  }

  @Test
  void deveRedefinirSenhaERevogarSessoes() {
    var usuario = mock(Usuario.class);
    var token = tokenValido(0);
    token.setUsuario(usuario);

    when(tokenRepository.findByUsuarioEmail("usuario@email.com")).thenReturn(
      Optional.of(token)
    );
    when(passwordEncoder.matches("123456", "codigo-hash")).thenReturn(true);
    when(passwordEncoder.encode("novaSenha123")).thenReturn("nova-senha-hash");

    service.redefinirSenha(
      new RedefinirSenhaDTO("usuario@email.com", "123456", "novaSenha123")
    );

    verify(usuario).setSenha("nova-senha-hash");
    verify(usuarioRepository).save(usuario);
    verify(refreshTokenService).revogarTodos(usuario);
    verify(tokenRepository).delete(token);
  }

  private PasswordResetToken tokenValido(int tentativas) {
    var token = new PasswordResetToken();
    token.setToken("codigo-hash");
    token.setTentativas(tentativas);
    token.setDataExpiracao(LocalDateTime.now().plusMinutes(15));
    return token;
  }
}
