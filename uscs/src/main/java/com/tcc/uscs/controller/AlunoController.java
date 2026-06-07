package com.tcc.uscs.controller;

import com.tcc.uscs.model.aluno.dto.*;
import com.tcc.uscs.service.AlunoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@RestController
@RequestMapping("alunos")
public class AlunoController {

  private final AlunoService service;

  @PostMapping
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
  public ResponseEntity<Page<ListarAlunoDTO>> listar(
    @PageableDefault(size = 10, sort = { "usuario.nome" }) Pageable paginacao
  ) {
    return ResponseEntity.ok(service.listar(paginacao));
  }

  @GetMapping("/{id}")
  public ResponseEntity<DetalharAlunoDTO> detalhar(@PathVariable Long id) {
    return ResponseEntity.ok(service.detalhar(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<DetalharAlunoDTO> atualizar(
    @PathVariable Long id,
    @RequestBody @Valid AtualizarAlunoDTO dados
  ) {
    return ResponseEntity.ok(service.atualizar(dados));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
