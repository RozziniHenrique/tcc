package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.avaliacao.Avaliacao;
import com.tcc.uscs.model.avaliacao.dto.CadastrarAvaliacaoDTO;
import com.tcc.uscs.model.avaliacao.dto.DetalharAvaliacaoDTO;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.AgendamentoRepository;
import com.tcc.uscs.repository.AvaliacaoRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

  private final AvaliacaoRepository avaliacaoRepository;
  private final AgendamentoRepository agendamentoRepository;

  @Transactional
  public DetalharAvaliacaoDTO avaliar(CadastrarAvaliacaoDTO dados) {
    var agendamento = agendamentoRepository
      .findById(dados.idAgendamento())
      .orElseThrow(() -> new ValidacaoException("Agendamento não encontrado."));

    if (!Boolean.TRUE.equals(agendamento.getAtivo())) {
      throw new ValidacaoException(
        "Não é possível avaliar um agendamento cancelado."
      );
    }

    var usuarioLogado = (Usuario) SecurityContextHolder.getContext()
      .getAuthentication()
      .getPrincipal();
    if (!agendamento.getCliente().getId().equals(usuarioLogado.getId())) {
      throw new AccessDeniedException(
        "Você só pode avaliar os seus próprios agendamentos."
      );
    }

    if (agendamento.getDataHora().isAfter(LocalDateTime.now())) {
      throw new ValidacaoException(
        "Você só pode avaliar um agendamento após a realização do serviço."
      );
    }

    if (avaliacaoRepository.existsByAgendamentoId(dados.idAgendamento())) {
      throw new ValidacaoException(
        "Este agendamento já foi avaliado anteriormente."
      );
    }

    var avaliacao = new Avaliacao(
      agendamento,
      dados.nota(),
      dados.comentario()
    );
    avaliacaoRepository.save(avaliacao);

    return new DetalharAvaliacaoDTO(avaliacao);
  }

  public DetalharAvaliacaoDTO buscarPorAgendamento(Long idAgendamento) {
    var avaliacao = avaliacaoRepository
      .findByAgendamentoId(idAgendamento)
      .orElseThrow(() ->
        new ValidacaoException(
          "Avaliação não encontrada para este agendamento."
        )
      );
    return new DetalharAvaliacaoDTO(avaliacao);
  }
}
