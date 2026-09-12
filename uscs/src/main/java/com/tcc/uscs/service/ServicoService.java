package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.servico.Servico;
import com.tcc.uscs.model.servico.dto.AtualizarServicoDTO;
import com.tcc.uscs.model.servico.dto.CadastrarServicoDTO;
import com.tcc.uscs.model.servico.dto.DetalharServicoDTO;
import com.tcc.uscs.model.servico.dto.ListarServicoDTO;
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

    var idsUnicos = ids.stream().distinct().toList();

    if (idsUnicos.size() != ids.size()) {
      throw new ValidacaoException(
        "A lista de serviços não pode conter IDs duplicados."
      );
    }

    var servicos = repository.findAllByIdInAndAtivoTrue(idsUnicos);

    if (servicos.size() != idsUnicos.size()) {
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
    var servico = repository
      .findByIdAndAtivoTrue(id)
      .orElseThrow(() ->
        new ValidacaoException("Serviço não encontrado ou inativo!")
      );
    return new DetalharServicoDTO(servico);
  }

  @Transactional
  public DetalharServicoDTO atualizar(Long id, AtualizarServicoDTO dados) {
    var servico = repository
      .findByIdAndAtivoTrue(id)
      .orElseThrow(() ->
        new ValidacaoException("Serviço não encontrado ou inativo!")
      );
    servico.atualizar(dados);
    return new DetalharServicoDTO(servico);
  }

  @Transactional
  public void excluir(Long id) {
    var servico = repository
      .findByIdAndAtivoTrue(id)
      .orElseThrow(() ->
        new ValidacaoException("Serviço não encontrado ou inativo!")
      );
    servico.excluir();
  }
}
