package com.tcc.uscs.service;

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

  public Page<ListarUnidadeDTO> listar(Pageable paginacao) {
    return repository.findAllByAtivoTrue(paginacao).map(ListarUnidadeDTO::new);
  }

  public DetalharUnidadeDTO detalhar(Long id) {
    var unidade = repository
      .findById(id)
      .orElseThrow(() -> new ValidacaoException("Unidade não encontrada!"));
    return new DetalharUnidadeDTO(unidade);
  }

  @Transactional
  public DetalharUnidadeDTO atualizar(Long id, AtualizarUnidadeDTO dados) {
    var unidade = repository
      .findById(id)
      .orElseThrow(() -> new ValidacaoException("Unidade não encontrada!"));
    unidade.atualizar(dados);
    return new DetalharUnidadeDTO(unidade);
  }

  @Transactional
  public void excluir(Long id) {
    var unidade = repository
      .findById(id)
      .orElseThrow(() -> new ValidacaoException("Unidade não encontrada!"));
    unidade.excluir();
  }
}
