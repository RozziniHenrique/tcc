package com.tcc.uscs.controller;

import com.tcc.uscs.model.cliente.dto.*;
import com.tcc.uscs.repository.ClienteRepository;
import com.tcc.uscs.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("clientes")
public class ClienteController {

  @Autowired
  private ClienteService service;

  @Autowired
  private ClienteRepository repository;

  @PostMapping
  public ResponseEntity cadastrar(
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
    var page = repository
      .findAllByUsuarioAtivoTrue(paginacao)
      .map(ListarClienteDTO::new);
    return ResponseEntity.ok(page);
  }

  @GetMapping("/{id}")
  public ResponseEntity detalhar(@PathVariable Long id) {
    var cliente = repository.getReferenceById(id);
    return ResponseEntity.ok(new DetalharClienteDTO(cliente));
  }

  @PutMapping
  public ResponseEntity atualizar(
    @RequestBody @Valid AtualizarClienteDTO dados
  ) {
    var detalhe = service.atualizar(dados);
    return ResponseEntity.ok(detalhe);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
