package com.tcc.uscs.controller;

import com.tcc.uscs.model.servico.dto.*;
import com.tcc.uscs.service.ServicoService;
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
@RequestMapping("servicos")
public class ServicoController {

  private final ServicoService service;

  @PostMapping
  @Operation(summary = "Cadastra Servico")
  public ResponseEntity<DetalharServicoDTO> cadastrar(
    @RequestBody @Valid CadastrarServicoDTO dados,
    UriComponentsBuilder uriBuilder
  ) {
    var detalhe = service.cadastrar(dados);
    var uri = uriBuilder
      .path("/servicos/{id}")
      .buildAndExpand(detalhe.id())
      .toUri();
    return ResponseEntity.created(uri).body(detalhe);
  }

  @GetMapping
  @Operation(summary = "Lista Servico")
  public ResponseEntity<Page<ListarServicoDTO>> listar(
    @PageableDefault(size = 10, sort = "nome") Pageable paginacao
  ) {
    return ResponseEntity.ok(service.listar(paginacao));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Detalha Servico")
  public ResponseEntity<DetalharServicoDTO> detalhar(@PathVariable Long id) {
    return ResponseEntity.ok(service.detalhar(id));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualiza Servico")
  public ResponseEntity<DetalharServicoDTO> atualizar(
    @PathVariable Long id,
    @RequestBody @Valid AtualizarServicoDTO dados
  ) {
    return ResponseEntity.ok(service.atualizar(id, dados));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Deleta Servico")
  public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
