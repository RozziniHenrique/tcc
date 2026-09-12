package com.tcc.uscs.model.curso.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AtualizarCursoDTO(
  @Size(min = 2, max = 255) String nome,
  @Size(min = 1, max = 2000) String descricao,
  @Size(min = 1, max = 50) String periodo,
  @Size(min = 1, max = 50) String duracao,
  @Pattern(regexp = "\\d{4}", message = "O ano vigente deve conter 4 dígitos")
  String anoVigente,
  @Positive BigDecimal valor
) {}
