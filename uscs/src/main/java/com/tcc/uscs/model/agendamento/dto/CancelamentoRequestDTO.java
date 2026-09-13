package com.tcc.uscs.model.agendamento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelamentoRequestDTO(
  @NotBlank(message = "A justificativa é obrigatória.")
  @Size(
    max = 255,
    message = "A justificativa deve conter no máximo 255 caracteres."
  )
  String justificativa
) {}
