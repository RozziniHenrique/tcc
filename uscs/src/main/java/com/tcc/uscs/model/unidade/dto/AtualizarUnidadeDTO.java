package com.tcc.uscs.model.unidade.dto;

import jakarta.validation.constraints.NotNull;

public record AtualizarUnidadeDTO(
  @NotNull Long id,
  String nome,
  String endereco,
  String cidade,
  String estado
) {}
