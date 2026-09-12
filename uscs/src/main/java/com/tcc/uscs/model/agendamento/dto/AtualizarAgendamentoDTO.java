package com.tcc.uscs.model.agendamento.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public record AtualizarAgendamentoDTO(
  @Positive Long idAluno,
  @Positive Long idCurso,
  @Positive Long idUnidade,
  @Size(min = 1) List<@Positive Long> idServicos,
  @Future LocalDateTime dataHora
) {
  public boolean semAlteracoes() {
    return (
      idAluno == null &&
      idCurso == null &&
      idUnidade == null &&
      idServicos == null &&
      dataHora == null
    );
  }
}
