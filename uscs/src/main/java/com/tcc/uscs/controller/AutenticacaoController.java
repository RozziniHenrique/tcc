package com.tcc.uscs.controller;

import com.tcc.uscs.infra.security.TokenService;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.model.usuario.dto.DadosAutenticacao;
import com.tcc.uscs.model.usuario.dto.DadosTokenJWT;
import com.tcc.uscs.model.usuario.dto.LogoutDTO;
import com.tcc.uscs.model.usuario.dto.RefreshTokenDTO;
import com.tcc.uscs.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AutenticacaoController {

  private final AuthenticationManager manager;
  private final TokenService tokenService;
  private final RefreshTokenService refreshTokenService;

  @PostMapping({ "/login", "/auth/login" })
  public ResponseEntity<DadosTokenJWT> autenticar(
    @RequestBody @Valid DadosAutenticacao dados
  ) {
    var authenticationToken = new UsernamePasswordAuthenticationToken(
      dados.email(),
      dados.senha()
    );

    var authentication = manager.authenticate(authenticationToken);
    var usuario = (Usuario) authentication.getPrincipal();

    return ResponseEntity.ok(gerarTokens(usuario));
  }

  @PostMapping("/auth/refresh")
  public ResponseEntity<DadosTokenJWT> renovar(
    @RequestBody @Valid RefreshTokenDTO dados
  ) {
    var usuario = refreshTokenService.validarEObterUsuario(
      dados.refreshToken()
    );

    refreshTokenService.revogar(dados.refreshToken());

    return ResponseEntity.ok(gerarTokens(usuario));
  }

  @PostMapping("/auth/logout")
  public ResponseEntity<Void> logout(@RequestBody @Valid LogoutDTO dados) {
    refreshTokenService.revogar(dados.refreshToken());
    return ResponseEntity.noContent().build();
  }

  private DadosTokenJWT gerarTokens(Usuario usuario) {
    var accessToken = tokenService.gerarAccessToken(usuario);
    var refreshToken = refreshTokenService.criar(usuario);

    return new DadosTokenJWT(
      accessToken,
      refreshToken,
      "Bearer",
      tokenService.getAccessExpirationSeconds()
    );
  }
}
