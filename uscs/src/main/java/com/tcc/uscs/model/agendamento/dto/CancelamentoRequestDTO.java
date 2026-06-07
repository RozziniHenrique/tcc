package com.tcc.uscs.model.agendamento.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelamentoRequestDTO(
  @NotBlank(message = "A justificativa é obrigatória.") String justificativa
) {}
