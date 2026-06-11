package com.tcc.uscs.controller;

import com.tcc.uscs.model.aluno.dto.*;
import com.tcc.uscs.service.AlunoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@RestController
@RequestMapping("alunos")
public class AlunoController {

  private final AlunoService service;

  @PostMapping
  @Operation(summary = "Cadastra Aluno")
  public ResponseEntity<DetalharAlunoDTO> cadastrar(
    @RequestBody @Valid CadastrarAlunoDTO dados,
    UriComponentsBuilder uriBuilder
  ) {
    var detalhe = service.cadastrar(dados);
    var uri = uriBuilder
      .path("/alunos/{id}")
      .buildAndExpand(detalhe.id())
      .toUri();
    return ResponseEntity.created(uri).body(detalhe);
  }

  @GetMapping
  @Operation(summary = "Lista Aluno")
  public ResponseEntity<Page<ListarAlunoDTO>> listar(
    @PageableDefault(size = 10, sort = { "usuario.nome" }) Pageable paginacao
  ) {
    return ResponseEntity.ok(service.listar(paginacao));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Detalha Aluno")
  public ResponseEntity<DetalharAlunoDTO> detalhar(@PathVariable Long id) {
    return ResponseEntity.ok(service.detalhar(id));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualiza Aluno")
  @Transactional
  public ResponseEntity<DetalharAlunoDTO> atualizar(
    @PathVariable Long id,
    @RequestBody @Valid AtualizarAlunoDTO dados
  ) {
    var dto = service.atualizar(id, dados);
    return ResponseEntity.ok(dto);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Deleta Aluno")
  public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
