package com.tcc.uscs.controller;

import com.tcc.uscs.model.funcionario.dto.*;
import com.tcc.uscs.repository.FuncionarioRepository;
import com.tcc.uscs.service.FuncionarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("funcionarios")
public class FuncionarioController {

  @Autowired
  private FuncionarioService service;

  @Autowired
  private FuncionarioRepository repository;

  @PostMapping
  public ResponseEntity cadastrar(
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
  public ResponseEntity<Page<ListarFuncionarioDTO>> listar(
    @PageableDefault(size = 10, sort = { "usuario.nome" }) Pageable paginacao
  ) {
    var page = repository
      .findAllByUsuarioAtivoTrue(paginacao)
      .map(ListarFuncionarioDTO::new);
    return ResponseEntity.ok(page);
  }

  @GetMapping("/{id}")
  public ResponseEntity detalhar(@PathVariable Long id) {
    var funcionario = repository.getReferenceById(id);
    return ResponseEntity.ok(new DetalharFuncionarioDTO(funcionario));
  }

  @PutMapping
  public ResponseEntity atualizar(
    @RequestBody @Valid AtualizarFuncionarioDTO dados
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
