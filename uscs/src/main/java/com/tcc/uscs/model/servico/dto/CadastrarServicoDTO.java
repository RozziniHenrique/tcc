package com.tcc.uscs.model.servico.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CadastrarServicoDTO(
  @NotBlank String nome,
  String descricao,
  @NotNull @Positive BigDecimal valor
) {}
