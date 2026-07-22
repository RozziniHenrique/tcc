package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.curso.Curso;
import com.tcc.uscs.model.curso.dto.AtualizarCursoDTO;
import com.tcc.uscs.model.curso.dto.CadastrarCursoDTO;
import com.tcc.uscs.model.curso.dto.DetalharCursoDTO;
import com.tcc.uscs.model.curso.dto.ListarCursoDTO;
import com.tcc.uscs.repository.CursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CursoService {

  private final CursoRepository repository;

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
    var curso = repository
      .findById(id)
      .orElseThrow(() -> new ValidacaoException("Curso não encontrado!"));
    return new DetalharCursoDTO(curso);
  }

  @Transactional
  public DetalharCursoDTO atualizar(Long id, AtualizarCursoDTO dados) {
    var curso = repository
      .findById(id)
      .orElseThrow(() -> new ValidacaoException("Curso não encontrado!"));
    curso.atualizar(dados);
    return new DetalharCursoDTO(curso);
  }

  @Transactional
  public void excluir(Long id) {
    var curso = repository
      .findById(id)
      .orElseThrow(() -> new ValidacaoException("Curso não encontrado!"));
    curso.excluir();
  }
}
