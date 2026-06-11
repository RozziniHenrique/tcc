package com.tcc.uscs.controller;

import com.tcc.uscs.model.curso.dto.*;
import com.tcc.uscs.service.CursoService;
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
@RequestMapping("cursos")
public class CursoController {

  private final CursoService service;

  @PostMapping
  @Transactional
  public ResponseEntity<DetalharCursoDTO> cadastrar(
    @RequestBody @Valid CadastrarCursoDTO dados,
    UriComponentsBuilder uriBuilder
  ) {
    var detalhe = service.cadastrar(dados);
    var uri = uriBuilder
      .path("/cursos/{id}")
      .buildAndExpand(detalhe.id())
      .toUri();
    return ResponseEntity.created(uri).body(detalhe);
  }

  @GetMapping
  public ResponseEntity<Page<ListarCursoDTO>> listar(
    @PageableDefault(size = 10, sort = { "nome" }) Pageable paginacao
  ) {
    return ResponseEntity.ok(service.listar(paginacao));
  }

  @GetMapping("/{id}")
  public ResponseEntity<DetalharCursoDTO> detalhar(@PathVariable Long id) {
    return ResponseEntity.ok(service.detalhar(id));
  }

  @PutMapping("/{id}")
  @Transactional
  public ResponseEntity<DetalharCursoDTO> atualizar(
    @PathVariable Long id,
    @RequestBody @Valid AtualizarCursoDTO dados
  ) {
    var dto = service.atualizar(id, dados);
    return ResponseEntity.ok(dto);
  }

  @DeleteMapping("/{id}")
  @Transactional
  public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
