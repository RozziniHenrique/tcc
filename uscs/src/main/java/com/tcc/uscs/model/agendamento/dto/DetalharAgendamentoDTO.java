package com.tcc.uscs.model.agendamento.dto;

import com.tcc.uscs.model.agendamento.Agendamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DetalharAgendamentoDTO(
  Long id,
  String nomeCliente,
  String nomeAluno,
  String nomeCurso,
  LocalDateTime dataHora,
  BigDecimal valorNoAto
) {
  public DetalharAgendamentoDTO(Agendamento a) {
    this(
      a.getId(),
      a.getCliente().getNome(),
      a.getAluno().getNome(),
      a.getCurso().getNome(),
      a.getDataHora(),
      a.getValorNoAto()
    );
  }
}
