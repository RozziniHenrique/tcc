package com.tcc.uscs.service;

import com.tcc.uscs.model.curso.Curso;
import com.tcc.uscs.model.curso.dto.*;
import com.tcc.uscs.repository.CursoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CursoService {

  @Autowired
  private CursoRepository repository;

  @Transactional
  public DetalharCursoDTO cadastrar(CadastrarCursoDTO dados) {
    var curso = new Curso(dados);
    repository.save(curso);
    return new DetalharCursoDTO(curso);
  }

  public Page<ListarCursoDTO> listar(Pageable paginacao) {
    return repository.findAllByAtivoTrue(paginacao).map(ListarCursoDTO::new);
  }

  public DetalharCursoDTO detalhar(Long id) {
    return new DetalharCursoDTO(repository.getReferenceById(id));
  }

  @Transactional
  public DetalharCursoDTO atualizar(AtualizarCursoDTO dados) {
    var curso = repository.getReferenceById(dados.id());
    curso.atualizar(dados);
    return new DetalharCursoDTO(curso);
  }

  @Transactional
  public void excluir(Long id) {
    repository.getReferenceById(id).excluir();
  }
}
