package com.tcc.uscs.service;

import com.tcc.uscs.model.unidade.Unidade;
import com.tcc.uscs.model.unidade.dto.*;
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
    return new DetalharUnidadeDTO(repository.getReferenceById(id));
  }

  @Transactional
  public DetalharUnidadeDTO atualizar(AtualizarUnidadeDTO dados) {
    var unidade = repository.getReferenceById(dados.id());
    unidade.atualizar(dados);
    return new DetalharUnidadeDTO(unidade);
  }

  @Transactional
  public void excluir(Long id) {
    repository.getReferenceById(id).excluir();
  }
}
