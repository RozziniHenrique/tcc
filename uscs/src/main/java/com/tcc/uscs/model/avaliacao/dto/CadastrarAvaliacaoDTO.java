package com.tcc.uscs.model.avaliacao.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CadastrarAvaliacaoDTO(
  @NotNull Long idAgendamento,
  @NotNull @Min(1) @Max(5) Integer nota,
  @Size(max = 500) String comentario
) {}
