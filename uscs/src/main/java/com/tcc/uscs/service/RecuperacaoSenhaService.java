package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.usuario.PasswordResetToken;
import com.tcc.uscs.model.usuario.dto.RedefinirSenhaDTO;
import com.tcc.uscs.model.usuario.dto.SolicitarRecuperacaoSenhaDTO;
import com.tcc.uscs.model.usuario.dto.VerificarCodigoSenhaDTO;
import com.tcc.uscs.repository.PasswordResetTokenRepository;
import com.tcc.uscs.repository.UsuarioRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecuperacaoSenhaService {

  private final UsuarioRepository usuarioRepository;
  private final PasswordResetTokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;
  private final RefreshTokenService refreshTokenService;
  private final SecureRandom secureRandom = new SecureRandom();

  @Value("${app.password-reset.expiration-minutes:15}")
  private long expirationMinutes;

  @Value("${app.password-reset.max-attempts:5}")
  private int maxAttempts;

  @Transactional
  public void solicitarRecuperacao(SolicitarRecuperacaoSenhaDTO dados) {
    // Resposta pública é sempre genérica para não revelar se o e-mail existe.
    var usuarioOpt = usuarioRepository.findByEmailAndAtivoTrue(dados.email());
    if (usuarioOpt.isEmpty()) {
      return;
    }

    var usuario = usuarioOpt.get();
    tokenRepository.deleteByUsuario(usuario);

    String codigo = String.format("%06d", secureRandom.nextInt(1_000_000));
    PasswordResetToken resetToken = new PasswordResetToken();
    resetToken.setToken(passwordEncoder.encode(codigo));
    resetToken.setUsuario(usuario);
    resetToken.setDataExpiracao(
      LocalDateTime.now().plusMinutes(expirationMinutes)
    );
    resetToken.setTentativas(0);
    tokenRepository.save(resetToken);

    emailService.enviarCodigoRecuperacao(
      usuario.getEmail(),
      codigo,
      expirationMinutes
    );
  }

  @Transactional(noRollbackFor = ValidacaoException.class)
  public boolean verificarCodigo(VerificarCodigoSenhaDTO dados) {
    var resetToken = obterTokenValido(dados.email());
    validarTentativa(resetToken, dados.codigo());
    return true;
  }

  @Transactional(noRollbackFor = ValidacaoException.class)
  public void redefinirSenha(RedefinirSenhaDTO dados) {
    var resetToken = obterTokenValido(dados.email());
    validarTentativa(resetToken, dados.codigo());

    var usuario = resetToken.getUsuario();
    usuario.setSenha(passwordEncoder.encode(dados.novaSenha()));
    usuarioRepository.save(usuario);
    refreshTokenService.revogarTodos(usuario);
    tokenRepository.delete(resetToken);
  }

  private PasswordResetToken obterTokenValido(String email) {
    var resetToken = tokenRepository
      .findByUsuarioEmail(email)
      .orElseThrow(() ->
        new ValidacaoException("Código inválido ou expirado.")
      );

    if (resetToken.isExpirado()) {
      tokenRepository.delete(resetToken);
      throw new ValidacaoException("Código inválido ou expirado.");
    }
    if (resetToken.getTentativas() >= maxAttempts) {
      tokenRepository.delete(resetToken);
      throw new ValidacaoException("Código inválido ou expirado.");
    }
    return resetToken;
  }

  private void validarTentativa(PasswordResetToken resetToken, String codigo) {
    if (!passwordEncoder.matches(codigo, resetToken.getToken())) {
      resetToken.setTentativas(resetToken.getTentativas() + 1);
      if (resetToken.getTentativas() >= maxAttempts) {
        tokenRepository.delete(resetToken);
      } else {
        tokenRepository.save(resetToken);
      }
      throw new ValidacaoException("Código inválido ou expirado.");
    }
  }
}
