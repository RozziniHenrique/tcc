package com.tcc.uscs.controller;

import com.tcc.uscs.model.cliente.dto.*;
import com.tcc.uscs.service.ClienteService;
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
@RequestMapping("clientes")
public class ClienteController {

  private final ClienteService service;

  @PostMapping
  @Operation(summary = "Cadastra Cliente")
  public ResponseEntity<DetalharClienteDTO> cadastrar(
    @RequestBody @Valid CadastrarClienteDTO dados,
    UriComponentsBuilder uriBuilder
  ) {
    var detalhe = service.cadastrar(dados);
    var uri = uriBuilder
      .path("/clientes/{id}")
      .buildAndExpand(detalhe.id())
      .toUri();
    return ResponseEntity.created(uri).body(detalhe);
  }

  @GetMapping
  @Operation(summary = "Lista Cliente")
  public ResponseEntity<Page<ListarClienteDTO>> listar(
    @PageableDefault(size = 10, sort = { "usuario.nome" }) Pageable paginacao
  ) {
    return ResponseEntity.ok(service.listar(paginacao));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Detalha Cliente")
  public ResponseEntity<DetalharClienteDTO> detalhar(@PathVariable Long id) {
    return ResponseEntity.ok(service.detalhar(id));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualiza Cliente")
  public ResponseEntity<DetalharClienteDTO> atualizar(
    @PathVariable Long id,
    @RequestBody @Valid AtualizarClienteDTO dados
  ) {
    var dto = service.atualizar(id, dados);
    return ResponseEntity.ok(dto);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Deleta Cliente")
  public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
