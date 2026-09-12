package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.TokenInvalidoException;
import com.tcc.uscs.model.usuario.RefreshToken;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenRepository repository;
  private final SecureRandom secureRandom = new SecureRandom();

  @Value("${api.security.token.refresh-expiration-days:30}")
  private long expirationDays;

  @Transactional
  public String criar(Usuario usuario) {
    byte[] bytes = new byte[48];
    secureRandom.nextBytes(bytes);
    String token = Base64.getUrlEncoder()
      .withoutPadding()
      .encodeToString(bytes);

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setTokenHash(hash(token));
    refreshToken.setUsuario(usuario);
    refreshToken.setDataExpiracao(LocalDateTime.now().plusDays(expirationDays));
    refreshToken.setRevogado(false);
    refreshToken.setCriadoEm(LocalDateTime.now());
    repository.save(refreshToken);

    return token;
  }

  @Transactional(readOnly = true)
  public Usuario validarEObterUsuario(String token) {
    var refreshToken = repository
      .findByTokenHashAndRevogadoFalse(hash(token))
      .orElseThrow(() -> new TokenInvalidoException("Refresh token inválido."));

    if (refreshToken.isExpirado() || !refreshToken.getUsuario().isEnabled()) {
      throw new TokenInvalidoException(
        "Refresh token expirado ou usuário inativo."
      );
    }

    return refreshToken.getUsuario();
  }

  @Transactional
  public void revogar(String token) {
    repository
      .findByTokenHashAndRevogadoFalse(hash(token))
      .ifPresent(rt -> {
        rt.setRevogado(true);
        repository.save(rt);
      });
  }

  @Transactional
  public void revogarTodos(Usuario usuario) {
    repository.deleteAllByUsuario(usuario);
  }

  private String hash(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(
        digest.digest(value.getBytes(StandardCharsets.UTF_8))
      );
    } catch (Exception ex) {
      throw new IllegalStateException(
        "Não foi possível processar o refresh token.",
        ex
      );
    }
  }
}
