package com.tcc.uscs.controller;

import com.tcc.uscs.model.usuario.dto.RedefinirSenhaDTO;
import com.tcc.uscs.model.usuario.dto.SolicitarRecuperacaoSenhaDTO;
import com.tcc.uscs.model.usuario.dto.VerificarCodigoSenhaDTO;
import com.tcc.uscs.service.RecuperacaoSenhaService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RecuperacaoSenhaController {

  private final RecuperacaoSenhaService service;

  @PostMapping({ "/auth/password/forgot", "/senha/solicitar-recuperacao" })
  public ResponseEntity<Map<String, String>> solicitar(
    @RequestBody @Valid SolicitarRecuperacaoSenhaDTO dados
  ) {
    service.solicitarRecuperacao(dados);
    return ResponseEntity.ok(
      Map.of(
        "mensagem",
        "Se o e-mail estiver cadastrado, um código de recuperação será enviado."
      )
    );
  }

  @PostMapping("/auth/password/verify")
  public ResponseEntity<Map<String, Boolean>> verificar(
    @RequestBody @Valid VerificarCodigoSenhaDTO dados
  ) {
    return ResponseEntity.ok(Map.of("valido", service.verificarCodigo(dados)));
  }

  @PostMapping({ "/auth/password/reset", "/senha/redefinir" })
  public ResponseEntity<Map<String, String>> redefinir(
    @RequestBody @Valid RedefinirSenhaDTO dados
  ) {
    service.redefinirSenha(dados);
    return ResponseEntity.ok(Map.of("mensagem", "Senha alterada com sucesso."));
  }
}
