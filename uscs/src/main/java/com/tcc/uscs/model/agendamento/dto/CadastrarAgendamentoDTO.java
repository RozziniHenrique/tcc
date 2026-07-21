package com.tcc.uscs.model.agendamento.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record CadastrarAgendamentoDTO(
  @NotNull Long idCliente,
  Long idAluno,
  @NotNull Long idCurso,
  @NotNull Long idUnidade,
  @NotEmpty List<Long> idServicos,
  @NotNull @Future LocalDateTime dataHora
) {}
