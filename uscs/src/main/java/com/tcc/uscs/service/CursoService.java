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
    if (
      repository.existsByNomeIgnoreCaseAndPeriodoIgnoreCaseAndAnoVigenteAndAtivoTrue(
        dados.nome(),
        dados.periodo(),
        dados.anoVigente()
      )
    ) {
      throw new ValidacaoException(
        "Já existe um curso ativo com o mesmo nome, período e ano vigente."
      );
    }

    var curso = new Curso(dados);
    repository.save(curso);
    return new DetalharCursoDTO(curso);
  }

  @Transactional(readOnly = true)
  public Page<ListarCursoDTO> listar(Pageable paginacao) {
    return repository.findAllByAtivoTrue(paginacao).map(ListarCursoDTO::new);
  }

  @Transactional(readOnly = true)
  public DetalharCursoDTO detalhar(Long id) {
    return new DetalharCursoDTO(obterCursoAtivo(id));
  }

  @Transactional
  public DetalharCursoDTO atualizar(Long id, AtualizarCursoDTO dados) {
    var curso = obterCursoAtivo(id);

    var nome = dados.nome() != null ? dados.nome() : curso.getNome();
    var periodo =
      dados.periodo() != null ? dados.periodo() : curso.getPeriodo();
    var anoVigente =
      dados.anoVigente() != null ? dados.anoVigente() : curso.getAnoVigente();

    if (
      repository.existsByNomeIgnoreCaseAndPeriodoIgnoreCaseAndAnoVigenteAndAtivoTrueAndIdNot(
        nome,
        periodo,
        anoVigente,
        id
      )
    ) {
      throw new ValidacaoException(
        "Já existe um curso ativo com o mesmo nome, período e ano vigente."
      );
    }

    curso.atualizar(dados);
    return new DetalharCursoDTO(curso);
  }

  @Transactional
  public void excluir(Long id) {
    obterCursoAtivo(id).excluir();
  }

  private Curso obterCursoAtivo(Long id) {
    return repository
      .findByIdAndAtivoTrue(id)
      .orElseThrow(() ->
        new ValidacaoException("Curso não encontrado ou inativo.")
      );
  }
}
