package com.tcc.uscs.controller;

import com.tcc.uscs.infra.security.TokenService;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.model.usuario.dto.DadosAutenticacao;
import com.tcc.uscs.model.usuario.dto.DadosTokenJWT;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class AutenticacaoController {

  private final AuthenticationManager manager;
  private final TokenService tokenService;

  @PostMapping
  public ResponseEntity<DadosTokenJWT> efuriaLogin(
    @RequestBody @Valid DadosAutenticacao dados
  ) {
    var authenticationToken = new UsernamePasswordAuthenticationToken(
      dados.email(),
      dados.senha()
    );

    var authentication = manager.authenticate(authenticationToken);

    var tokenJWT = tokenService.gerarToken(
      (Usuario) authentication.getPrincipal()
    );

    return ResponseEntity.ok(new DadosTokenJWT(tokenJWT));
  }
}
