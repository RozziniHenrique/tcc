package com.tcc.uscs.controller;

import com.tcc.uscs.model.usuario.dto.DadosCadastroUsuario;
import com.tcc.uscs.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

  private final UsuarioService service;

  @PostMapping
  public ResponseEntity<String> cadastrar(
    @RequestBody @Valid DadosCadastroUsuario dados,
    UriComponentsBuilder uriBuilder
  ) {
    Long idGerado = service.cadastrar(dados);

    var uri = uriBuilder
      .path("/usuarios/{id}")
      .buildAndExpand(idGerado)
      .toUri();

    return ResponseEntity.created(uri).body("Usuário cadastrado com sucesso!");
  }
}
