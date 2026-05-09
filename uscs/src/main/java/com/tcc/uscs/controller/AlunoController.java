package com.tcc.uscs.controller;

import com.tcc.uscs.model.aluno.dto.*;
import com.tcc.uscs.service.AlunoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("alunos")
public class AlunoController {

  @Autowired
  private AlunoService service;

  @PostMapping
  public ResponseEntity cadastrar(
    @RequestBody @Valid CadastrarAlunoDTO dados,
    UriComponentsBuilder uriBuilder
  ) {
    var detalhe = service.cadastrar(dados);
    var uri = uriBuilder
      .path("/alunos/{id}")
      .buildAndExpand(detalhe.id())
      .toUri();
    return ResponseEntity.created(uri).body(detalhe);
  }

  @GetMapping
  public ResponseEntity<Page<ListarAlunoDTO>> listar(
    @PageableDefault(size = 10, sort = { "usuario.nome" }) Pageable paginacao
  ) {
    return ResponseEntity.ok(service.listar(paginacao));
  }

  @GetMapping("/{id}")
  public ResponseEntity detalhar(@PathVariable Long id) {
    return ResponseEntity.ok(service.detalhar(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity atualizar(
    @PathVariable Long id,
    @RequestBody @Valid AtualizarAlunoDTO dados
  ) {
    return ResponseEntity.ok(service.atualizar(dados));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
