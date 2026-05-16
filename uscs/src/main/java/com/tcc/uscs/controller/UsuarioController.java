package com.tcc.uscs.controller;

import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.model.usuario.dto.DadosCadastroUsuario;
import com.tcc.uscs.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

  private final UsuarioRepository repository;
  private final PasswordEncoder passwordEncoder;

  @PostMapping
  @Transactional
  public ResponseEntity cadastrar(
    @RequestBody @Valid DadosCadastroUsuario dados,
    UriComponentsBuilder uriBuilder
  ) {
    String senhaCriptografada = passwordEncoder.encode(dados.senha());

    var usuario = new Usuario(dados, senhaCriptografada);

    repository.save(usuario);

    var uri = uriBuilder
      .path("/usuarios/{id}")
      .buildAndExpand(usuario.getId())
      .toUri();

    return ResponseEntity.created(uri).body("Usuário cadastrado com sucesso!");
  }
}
