package com.tcc.uscs.model.curso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CadastrarCursoDTO(
  @NotBlank @Size(min = 2, max = 255) String nome,
  @NotBlank @Size(max = 2000) String descricao,
  @NotBlank @Size(max = 50) String periodo,
  @NotBlank @Size(max = 50) String duracao,
  @NotBlank
  @Pattern(regexp = "\\d{4}", message = "O ano vigente deve conter 4 dígitos")
  String anoVigente,
  @NotNull @Positive BigDecimal valor
) {}
