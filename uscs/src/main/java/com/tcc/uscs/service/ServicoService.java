package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.servico.Servico;
import com.tcc.uscs.model.servico.dto.*;
import com.tcc.uscs.repository.ServicoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ServicoService {

  private final ServicoRepository repository;

  public List<Servico> buscarServicosValidos(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      throw new ValidacaoException("A lista de serviços não pode estar vazia.");
    }

    List<Servico> servicos = repository.findAllById(ids);

    if (servicos.size() != ids.size()) {
      throw new ValidacaoException(
        "Um ou mais serviços informados não foram encontrados ou estão inativos!"
      );
    }

    return servicos;
  }

  @Transactional
  public DetalharServicoDTO cadastrar(CadastrarServicoDTO dados) {
    var servico = new Servico(dados);
    repository.save(servico);
    return new DetalharServicoDTO(servico);
  }

  public Page<ListarServicoDTO> listar(Pageable paginacao) {
    return repository.findAllByAtivoTrue(paginacao).map(ListarServicoDTO::new);
  }

  public DetalharServicoDTO detalhar(Long id) {
    return new DetalharServicoDTO(repository.getReferenceById(id));
  }

  @Transactional
  public DetalharServicoDTO atualizar(Long id, AtualizarServicoDTO dados) {
    var servico = repository.getReferenceById(id);
    servico.atualizar(dados);
    return new DetalharServicoDTO(servico);
  }

  @Transactional
  public void excluir(Long id) {
    repository.getReferenceById(id).excluir();
  }
}
