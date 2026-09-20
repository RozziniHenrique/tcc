package com.tcc.uscs.model.agendamento.dto;

import com.tcc.uscs.model.agendamento.StatusAgendamento;
import java.time.LocalDate;

public record FiltroAgendamentoDTO(
  StatusAgendamento status,
  LocalDate inicio,
  LocalDate fim,
  Long idCurso,
  Long idAluno,
  Long idCliente,
  Long idUnidade
) {
  public static FiltroAgendamentoDTO vazio() {
    return new FiltroAgendamentoDTO(
      null,
      null,
      null,
      null,
      null,
      null,
      null
    );
  }
}
