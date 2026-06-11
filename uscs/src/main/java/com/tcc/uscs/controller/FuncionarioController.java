package com.tcc.uscs.controller;

import com.tcc.uscs.model.funcionario.dto.*;
import com.tcc.uscs.service.FuncionarioService;
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
@RequestMapping("funcionarios")
public class FuncionarioController {

  private final FuncionarioService service;

  @PostMapping
  @Operation(summary = "Cadastra Funcionario")
  public ResponseEntity<DetalharFuncionarioDTO> cadastrar(
    @RequestBody @Valid CadastrarFuncionarioDTO dados,
    UriComponentsBuilder uriBuilder
  ) {
    var detalhe = service.cadastrar(dados);
    var uri = uriBuilder
      .path("/funcionarios/{id}")
      .buildAndExpand(detalhe.id())
      .toUri();
    return ResponseEntity.created(uri).body(detalhe);
  }

  @GetMapping
  @Operation(summary = "Lista Funcionario")
  public ResponseEntity<Page<ListarFuncionarioDTO>> listar(
    @PageableDefault(size = 10, sort = { "usuario.nome" }) Pageable paginacao
  ) {
    return ResponseEntity.ok(service.listar(paginacao));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Detalha Funcionario")
  public ResponseEntity<DetalharFuncionarioDTO> detalhar(
    @PathVariable Long id
  ) {
    return ResponseEntity.ok(service.detalhar(id));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualiza Funcionario")
  public ResponseEntity<DetalharFuncionarioDTO> atualizar(
    @PathVariable Long id,
    @RequestBody @Valid AtualizarFuncionarioDTO dados
  ) {
    var dto = service.atualizar(id, dados);
    return ResponseEntity.ok(dto);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Deleta Funcionario")
  public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
