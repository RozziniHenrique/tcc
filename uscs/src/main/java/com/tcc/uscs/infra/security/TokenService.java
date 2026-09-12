package com.tcc.uscs.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.tcc.uscs.infra.exception.TokenInvalidoException;
import com.tcc.uscs.model.usuario.Usuario;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  @Value("${api.security.token.secret}")
  private String secret;

  @Value("${api.security.token.access-expiration-seconds:900}")
  private long accessExpirationSeconds;

  private static final String ISSUER = "STFER API";

  public String gerarAccessToken(Usuario usuario) {
    try {
      var algoritmo = Algorithm.HMAC256(secret);
      List<String> roles = usuario
        .getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .toList();

      return JWT.create()
        .withIssuer(ISSUER)
        .withSubject(usuario.getEmail())
        .withClaim("id", usuario.getId())
        .withClaim("roles", roles)
        .withIssuedAt(Instant.now())
        .withExpiresAt(Instant.now().plusSeconds(accessExpirationSeconds))
        .sign(algoritmo);
    } catch (JWTCreationException exception) {
      throw new IllegalStateException("Erro ao gerar token JWT", exception);
    }
  }

  public String gerarToken(Usuario usuario) {
    return gerarAccessToken(usuario);
  }

  public String getSubject(String tokenJWT) {
    try {
      var algoritmo = Algorithm.HMAC256(secret);
      return JWT.require(algoritmo)
        .withIssuer(ISSUER)
        .build()
        .verify(tokenJWT)
        .getSubject();
    } catch (JWTVerificationException exception) {
      throw new TokenInvalidoException("Token JWT inválido ou expirado!");
    }
  }

  public long getAccessExpirationSeconds() {
    return accessExpirationSeconds;
  }
}
