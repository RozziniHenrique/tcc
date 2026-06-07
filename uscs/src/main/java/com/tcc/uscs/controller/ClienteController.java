package com.tcc.uscs.controller;

import com.tcc.uscs.model.cliente.dto.*;
import com.tcc.uscs.service.ClienteService;
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
  public ResponseEntity<Page<ListarClienteDTO>> listar(
    @PageableDefault(size = 10, sort = { "usuario.nome" }) Pageable paginacao
  ) {
    return ResponseEntity.ok(service.listar(paginacao));
  }

  @GetMapping("/{id}")
  public ResponseEntity<DetalharClienteDTO> detalhar(@PathVariable Long id) {
    return ResponseEntity.ok(service.detalhar(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<DetalharClienteDTO> atualizar(
    @PathVariable Long id,
    @RequestBody @Valid AtualizarClienteDTO dados
  ) {
    return ResponseEntity.ok(service.atualizar(dados));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
