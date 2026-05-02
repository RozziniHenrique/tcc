package com.tcc.uscs.controller;

import com.tcc.uscs.model.funcionario.Funcionario;
import com.tcc.uscs.model.funcionario.dto.*;
import com.tcc.uscs.repository.FuncionarioRepository;
import jakarta.transaction.Transactional;
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
  private FuncionarioRepository repository;

  @PostMapping
  @Transactional
  public ResponseEntity cadastrar(
    @RequestBody @Valid CadastrarFuncionarioDTO dados,
    UriComponentsBuilder uriBuilder
  ) {
    var funcionario = new Funcionario(dados);
    repository.save(funcionario);
    var uri = uriBuilder
      .path("/funcionarios/{id}")
      .buildAndExpand(funcionario.getId())
      .toUri();
    return ResponseEntity.created(uri).body(
      new DetalharFuncionarioDTO(funcionario)
    );
  }

  @GetMapping
  public ResponseEntity<Page<ListarFuncionarioDTO>> listar(
    @PageableDefault(size = 10) Pageable paginacao
  ) {
    var page = repository
      .findAllByAtivoTrue(paginacao)
      .map(ListarFuncionarioDTO::new);
    return ResponseEntity.ok(page);
  }

  @PutMapping
  @Transactional
  public ResponseEntity atualizar(
    @RequestBody @Valid AtualizarFuncionarioDTO dados
  ) {
    var funcionario = repository.getReferenceById(dados.id());
    funcionario.atualizar(dados);
    return ResponseEntity.ok(new DetalharFuncionarioDTO(funcionario));
  }

  @DeleteMapping("/{id}")
  @Transactional
  public ResponseEntity excluir(@PathVariable Long id) {
    var funcionario = repository.getReferenceById(id);
    funcionario.excluir();
    return ResponseEntity.noContent().build();
  }
}
