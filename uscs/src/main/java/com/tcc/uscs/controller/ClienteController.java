package com.tcc.uscs.controller;

import com.tcc.uscs.model.cliente.*;
import com.tcc.uscs.model.cliente.dto.*;
import com.tcc.uscs.repository.ClienteRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("clientes")
public class ClienteController {

  @Autowired
  private ClienteRepository repository;

  @PostMapping
  @Transactional
  public ResponseEntity cadastrar(
    @RequestBody @Valid CadastrarClienteDTO dados,
    UriComponentsBuilder uriBuilder
  ) {
    var cliente = new Cliente(dados);
    repository.save(cliente);

    var uri = uriBuilder
      .path("/clientes/{id}")
      .buildAndExpand(cliente.getId())
      .toUri();
    return ResponseEntity.created(uri).body(new DetalharClienteDTO(cliente));
  }

  @GetMapping
  public ResponseEntity<Page<ListarClienteDTO>> listar(
    @PageableDefault(size = 10, sort = { "nome" }) Pageable paginacao
  ) {
    var page = repository
      .findAllByAtivoTrue(paginacao)
      .map(ListarClienteDTO::new);
    return ResponseEntity.ok(page);
  }

  @GetMapping("/{id}")
  public ResponseEntity detalhar(@PathVariable Long id) {
    var cliente = repository.getReferenceById(id);
    return ResponseEntity.ok(new DetalharClienteDTO(cliente));
  }

  @PutMapping
  @Transactional
  public ResponseEntity atualizar(
    @RequestBody @Valid AtualizarClienteDTO dados
  ) {
    var cliente = repository.getReferenceById(dados.id());
    cliente.atualizar(dados);
    return ResponseEntity.ok(new DetalharClienteDTO(cliente));
  }

  @DeleteMapping("/{id}")
  @Transactional
  public ResponseEntity excluir(@PathVariable Long id) {
    var cliente = repository.getReferenceById(id);
    cliente.excluir();
    return ResponseEntity.noContent().build();
  }
}
