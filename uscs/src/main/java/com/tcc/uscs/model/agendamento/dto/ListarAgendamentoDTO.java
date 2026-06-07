package com.tcc.uscs.model.agendamento.dto;

import com.tcc.uscs.model.agendamento.Agendamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ListarAgendamentoDTO(
  Long id,
  String nomeCliente,
  String nomeAluno,
  String nomeCurso,
  String nomeUnidade,
  LocalDateTime dataHora,
  BigDecimal valorNoAto,
  Boolean ativo
) {
  public ListarAgendamentoDTO(Agendamento a) {
    this(
      a.getId(),
      a.getCliente().getUsuario().getNome(),
      a.getAluno().getUsuario().getNome(),
      a.getCurso().getNome(),
      a.getUnidade() != null ? a.getUnidade().getNome() : "Não informada",
      a.getDataHora(),
      a.getValorNoAto(),
      a.getAtivo()
    );
  }
}
