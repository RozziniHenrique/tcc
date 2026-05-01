package com.tcc.uscs.model.agendamento.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record CadastrarAgendamentoDTO(
  @NotNull Long idCliente,
  @NotNull Long idAluno,
  @NotNull Long idCurso,
  @NotNull @Future LocalDateTime dataHora
) {}
