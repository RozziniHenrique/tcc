package com.tcc.uscs.model.agendamento.dto;

import com.tcc.uscs.model.agendamento.Agendamento;
import com.tcc.uscs.model.agendamento.StatusAgendamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DetalharAgendamentoDTO(
  Long id,
  Long idCliente,
  String nomeCliente,
  Long idAluno,
  String nomeAluno,
  Long idCurso,
  String nomeCurso,
  Long idUnidade,
  String nomeUnidade,
  List<ServicoAgendamentoDTO> servicos,
  LocalDateTime dataHora,
  BigDecimal valorNoAto,
  StatusAgendamento status,
  String justificativaCancelamento
) {
  public DetalharAgendamentoDTO(Agendamento a) {
    this(
      a.getId(),
      a.getCliente().getId(),
      a.getCliente().getUsuario().getNome(),
      a.getAluno().getId(),
      a.getAluno().getUsuario().getNome(),
      a.getCurso().getId(),
      a.getCurso().getNome(),
      a.getUnidade() != null ? a.getUnidade().getId() : null,
      a.getUnidade() != null ? a.getUnidade().getNome() : "Não informada",
      a.getServicos() == null
        ? List.of()
        : a.getServicos().stream().map(ServicoAgendamentoDTO::new).toList(),
      a.getDataHora(),
      a.getValorNoAto(),
      a.getStatus(),
      a.getJustificativaCancelamento()
    );
  }
}
