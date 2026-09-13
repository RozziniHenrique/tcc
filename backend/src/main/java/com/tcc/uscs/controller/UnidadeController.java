package com.tcc.uscs.controller;

import com.tcc.uscs.model.unidade.dto.*;
import com.tcc.uscs.service.UnidadeService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("unidades")
public class UnidadeController {

  private final UnidadeService service;

  @PostMapping
  @Operation(summary = "Cadastra Unidade")
  public ResponseEntity<DetalharUnidadeDTO> cadastrar(
    @RequestBody @Valid CadastrarUnidadeDTO dados,
    UriComponentsBuilder uriBuilder
  ) {
    var detalhe = service.cadastrar(dados);
    var uri = uriBuilder
      .path("/unidades/{id}")
      .buildAndExpand(detalhe.id())
      .toUri();
    return ResponseEntity.created(uri).body(detalhe);
  }

  @GetMapping
  @Operation(summary = "Lista Unidade")
  public ResponseEntity<Page<ListarUnidadeDTO>> listar(
    @PageableDefault(size = 10, sort = "nome") Pageable paginacao
  ) {
    return ResponseEntity.ok(service.listar(paginacao));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Detalha Unidade")
  public ResponseEntity<DetalharUnidadeDTO> detalhar(@PathVariable Long id) {
    return ResponseEntity.ok(service.detalhar(id));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualiza Unidade")
  public ResponseEntity<DetalharUnidadeDTO> atualizar(
    @PathVariable Long id,
    @RequestBody @Valid AtualizarUnidadeDTO dados
  ) {
    return ResponseEntity.ok(service.atualizar(id, dados));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Deleta Unidade")
  public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
