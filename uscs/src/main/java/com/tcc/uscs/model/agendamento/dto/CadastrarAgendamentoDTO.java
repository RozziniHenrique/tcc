package com.tcc.uscs.model.agendamento.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;

public record CadastrarAgendamentoDTO(
  @NotNull @Positive Long idCliente,
  @Positive Long idAluno,
  @NotNull @Positive Long idCurso,
  @NotNull @Positive Long idUnidade,
  @NotEmpty List<@Positive Long> idServicos,
  @NotNull @Future LocalDateTime dataHora
) {}
