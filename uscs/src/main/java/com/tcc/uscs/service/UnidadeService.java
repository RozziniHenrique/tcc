package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.RecursoNaoEncontradoException;
import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.unidade.Unidade;
import com.tcc.uscs.model.unidade.dto.AtualizarUnidadeDTO;
import com.tcc.uscs.model.unidade.dto.CadastrarUnidadeDTO;
import com.tcc.uscs.model.unidade.dto.DetalharUnidadeDTO;
import com.tcc.uscs.model.unidade.dto.ListarUnidadeDTO;
import com.tcc.uscs.repository.UnidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UnidadeService {

  private final UnidadeRepository repository;

  @Transactional
  public DetalharUnidadeDTO cadastrar(CadastrarUnidadeDTO dados) {
    var unidade = new Unidade(dados);
    repository.save(unidade);
    return new DetalharUnidadeDTO(unidade);
  }

  @Transactional(readOnly = true)
  public Page<ListarUnidadeDTO> listar(Pageable paginacao) {
    return repository.findAllByAtivoTrue(paginacao).map(ListarUnidadeDTO::new);
  }

  @Transactional(readOnly = true)
  public DetalharUnidadeDTO detalhar(Long id) {
    return new DetalharUnidadeDTO(obterUnidadeAtiva(id));
  }

  @Transactional
  public DetalharUnidadeDTO atualizar(Long id, AtualizarUnidadeDTO dados) {
    if (dados.semAlteracoes()) {
      throw new ValidacaoException(
        "Informe pelo menos um campo para realizar a atualização."
      );
    }
    var unidade = obterUnidadeAtiva(id);
    unidade.atualizar(dados);
    return new DetalharUnidadeDTO(unidade);
  }

  @Transactional
  public void excluir(Long id) {
    obterUnidadeAtiva(id).excluir();
  }

  private Unidade obterUnidadeAtiva(Long id) {
    return repository
      .findByIdAndAtivoTrue(id)
      .orElseThrow(() ->
        new RecursoNaoEncontradoException("Unidade não encontrada ou inativa!")
      );
  }
}
