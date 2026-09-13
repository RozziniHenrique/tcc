package com.tcc.uscs.model.servico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CadastrarServicoDTO(
  @NotBlank @Size(max = 150) String nome,
  @Size(max = 2000) String descricao,
  @NotNull @Positive BigDecimal valor
) {}
