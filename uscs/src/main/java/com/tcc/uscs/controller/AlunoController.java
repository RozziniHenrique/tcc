package com.tcc.uscs.controller;

import com.tcc.uscs.model.aluno.*;
import com.tcc.uscs.model.aluno.dto.*;
import com.tcc.uscs.repository.AlunoRepository;
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
@RequestMapping("alunos")
public class AlunoController {

  @Autowired
  private AlunoRepository repository;

  @Autowired
  private CursoRepository cursoRepository;

  @PostMapping
  @Transactional
  public ResponseEntity cadastrar(
    @RequestBody @Valid CadastrarAlunoDTO dados,
    UriComponentsBuilder uriBuilder
  ) {
    var curso = cursoRepository.getReferenceById(dados.idCurso());
    var aluno = new Aluno(dados, curso);
    repository.save(aluno);

    var uri = uriBuilder
      .path("/alunos/{id}")
      .buildAndExpand(aluno.getId())
      .toUri();
    return ResponseEntity.created(uri).body(new DetalharAlunoDTO(aluno));
  }

  @GetMapping
  public ResponseEntity<Page<ListarAlunoDTO>> listar(
    @PageableDefault(size = 10, sort = { "nome" }) Pageable paginacao
  ) {
    var page = repository
      .findAllByAtivoTrue(paginacao)
      .map(ListarAlunoDTO::new);
    return ResponseEntity.ok(page);
  }

  @GetMapping("/{id}")
  public ResponseEntity detalhar(@PathVariable Long id) {
    var aluno = repository.getReferenceById(id);
    return ResponseEntity.ok(new DetalharAlunoDTO(aluno));
  }

  @PutMapping
  @Transactional
  public ResponseEntity atualizar(@RequestBody @Valid AtualizarAlunoDTO dados) {
    var aluno = repository.getReferenceById(dados.id());
    aluno.atualizar(dados);
    return ResponseEntity.ok(new DetalharAlunoDTO(aluno));
  }

  @DeleteMapping("/{id}")
  @Transactional
  public ResponseEntity excluir(@PathVariable Long id) {
    var aluno = repository.getReferenceById(id);
    aluno.excluir();
    return ResponseEntity.noContent().build();
  }
}
