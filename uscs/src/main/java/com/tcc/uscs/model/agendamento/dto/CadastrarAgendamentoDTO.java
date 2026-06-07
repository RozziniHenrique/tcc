package com.tcc.uscs.model.agendamento.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public record CadastrarAgendamentoDTO(
  @NotNull Long idCliente,
  @NotNull Long idAluno,
  @NotNull Long idCurso,
  @NotNull Long idUnidade,
  @NotEmpty List<Long> idServicos,
  @NotNull @Future LocalDateTime dataHora
) {}
