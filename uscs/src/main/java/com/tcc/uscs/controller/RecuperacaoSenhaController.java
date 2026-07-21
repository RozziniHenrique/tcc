package com.tcc.uscs.controller;

import com.tcc.uscs.model.usuario.dto.RedefinirSenhaDTO;
import com.tcc.uscs.model.usuario.dto.SolicitarRecuperacaoSenhaDTO;
import com.tcc.uscs.service.RecuperacaoSenhaService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/senha")
@RequiredArgsConstructor
public class RecuperacaoSenhaController {

  private final RecuperacaoSenhaService service;

  @PostMapping("/solicitar-recuperacao")
  public ResponseEntity<Map<String, String>> solicitar(
    @RequestBody @Valid SolicitarRecuperacaoSenhaDTO dados
  ) {
    String token = service.solicitarRecuperacao(dados);
    return ResponseEntity.ok(
      Map.of(
        "mensagem",
        "Solicitação processada com sucesso.",
        "tokenDeTeste",
        token
      )
    );
  }

  @PostMapping("/redefinir")
  public ResponseEntity<Map<String, String>> redefinir(
    @RequestBody @Valid RedefinirSenhaDTO dados
  ) {
    service.redefinirSenha(dados);
    return ResponseEntity.ok(Map.of("mensagem", "Senha alterada com sucesso."));
  }
}
