package com.tcc.uscs.model.unidade.dto;

import com.tcc.uscs.model.unidade.Unidade;

public record ListarUnidadeDTO(
  Long id,
  String nome,
  String cidade,
  String estado
) {
  public ListarUnidadeDTO(Unidade u) {
    this(u.getId(), u.getNome(), u.getCidade(), u.getEstado());
  }
}
