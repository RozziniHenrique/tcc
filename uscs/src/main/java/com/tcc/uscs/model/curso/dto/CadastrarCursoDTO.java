package com.tcc.uscs.model.curso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CadastrarCursoDTO(
  @NotBlank String nome,
  @NotBlank String descricao,
  @NotBlank String periodo,
  @NotBlank String duracao,
  @NotBlank String anoVigente,
  @NotNull @Positive BigDecimal valor
) {}
