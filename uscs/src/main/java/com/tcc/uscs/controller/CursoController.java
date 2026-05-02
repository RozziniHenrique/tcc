package com.tcc.uscs.controller;

import com.tcc.uscs.model.curso.*;
import com.tcc.uscs.model.curso.dto.*;
import com.tcc.uscs.repository.CursoRepository;
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
@RequestMapping("cursos")
public class CursoController {

  @Autowired
  private CursoRepository repository;

  @PostMapping
  @Transactional
  public ResponseEntity cadastrar(
    @RequestBody @Valid CadastrarCursoDTO dados,
    UriComponentsBuilder uriBuilder
  ) {
    var curso = new Curso(dados);
    repository.save(curso);

    var uri = uriBuilder
      .path("/cursos/{id}")
      .buildAndExpand(curso.getId())
      .toUri();
    return ResponseEntity.created(uri).body(new DetalharCursoDTO(curso));
  }

  @GetMapping
  public ResponseEntity<Page<ListarCursoDTO>> listar(
    @PageableDefault(size = 10, sort = { "nome" }) Pageable paginacao
  ) {
    var page = repository
      .findAllByAtivoTrue(paginacao)
      .map(ListarCursoDTO::new);
    return ResponseEntity.ok(page);
  }

  @GetMapping("/{id}")
  public ResponseEntity detalhar(@PathVariable Long id) {
    var curso = repository.getReferenceById(id);
    return ResponseEntity.ok(new DetalharCursoDTO(curso));
  }

  @PutMapping
  @Transactional
  public ResponseEntity atualizar(@RequestBody @Valid AtualizarCursoDTO dados) {
    var curso = repository.getReferenceById(dados.id());
    curso.atualizar(dados);
    return ResponseEntity.ok(new DetalharCursoDTO(curso));
  }

  @DeleteMapping("/{id}")
  @Transactional
  public ResponseEntity excluir(@PathVariable Long id) {
    var curso = repository.getReferenceById(id);
    curso.excluir();
    return ResponseEntity.noContent().build();
  }
}
