package com.tcc.uscs.model.unidade.dto;

import com.tcc.uscs.model.unidade.Unidade;

public record DetalharUnidadeDTO(
  Long id,
  String nome,
  String endereco,
  String cidade,
  String estado,
  Boolean ativo
) {
  public DetalharUnidadeDTO(Unidade u) {
    this(
      u.getId(),
      u.getNome(),
      u.getEndereco(),
      u.getCidade(),
      u.getEstado(),
      u.getAtivo()
    );
  }
}
