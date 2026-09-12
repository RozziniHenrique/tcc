package com.tcc.uscs.model.servico.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AtualizarServicoDTO(
  @Size(min = 2, max = 150) String nome,
  @Size(max = 2000) String descricao,
  @Positive BigDecimal valor
) {}
