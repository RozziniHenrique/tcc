package com.tcc.uscs.model.servico.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AtualizarServicoDTO(
  @NotNull Long id,
  String nome,
  String descricao,
  BigDecimal valor
) {}
