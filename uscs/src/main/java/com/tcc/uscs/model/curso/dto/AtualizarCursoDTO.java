package com.tcc.uscs.model.curso.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AtualizarCursoDTO(
  @NotNull Long id,
  String nome,
  String descricao,
  String periodo,
  String duracao,
  String anoVigente,
  BigDecimal valor
) {}
