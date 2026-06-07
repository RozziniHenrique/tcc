package com.tcc.uscs.model.agendamento.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AtualizarAgendamentoDTO(
  @NotNull Long id,
  Long idCliente,
  Long idAluno,
  Long idCurso,
  Long idUnidade,
  @Future LocalDateTime dataHora,
  BigDecimal valorNoAto
) {}
