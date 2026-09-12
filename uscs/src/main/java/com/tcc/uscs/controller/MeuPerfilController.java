package com.tcc.uscs.controller;

import com.tcc.uscs.model.me.dto.AdicionarPerfilAlunoDTO;
import com.tcc.uscs.model.me.dto.AdicionarPerfilClienteDTO;
import com.tcc.uscs.model.me.dto.AtualizarMeuPerfilDTO;
import com.tcc.uscs.model.me.dto.MeuPerfilDTO;
import com.tcc.uscs.service.MeuPerfilService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class MeuPerfilController {

  private final MeuPerfilService service;

  @GetMapping
  @Operation(summary = "Retorna os dados do usuário autenticado")
  public ResponseEntity<MeuPerfilDTO> detalhar() {
    return ResponseEntity.ok(service.detalhar());
  }

  @PutMapping
  @Operation(summary = "Atualiza os dados básicos do usuário autenticado")
  public ResponseEntity<MeuPerfilDTO> atualizar(
    @RequestBody @Valid AtualizarMeuPerfilDTO dados
  ) {
    return ResponseEntity.ok(service.atualizar(dados));
  }

  @PostMapping("/perfis/cliente")
  @Operation(summary = "Adiciona o perfil de cliente à conta autenticada")
  public ResponseEntity<MeuPerfilDTO> adicionarPerfilCliente(
    @RequestBody @Valid AdicionarPerfilClienteDTO dados
  ) {
    return ResponseEntity.ok(service.adicionarPerfilCliente(dados));
  }

  @PostMapping("/perfis/aluno")
  @Operation(summary = "Adiciona o perfil de aluno à conta autenticada")
  public ResponseEntity<MeuPerfilDTO> adicionarPerfilAluno(
    @RequestBody @Valid AdicionarPerfilAlunoDTO dados
  ) {
    return ResponseEntity.ok(service.adicionarPerfilAluno(dados));
  }
}
