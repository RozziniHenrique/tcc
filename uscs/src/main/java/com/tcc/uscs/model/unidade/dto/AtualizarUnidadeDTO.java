package com.tcc.uscs.model.unidade.dto;

import jakarta.validation.constraints.Size;

public record AtualizarUnidadeDTO(
  @Size(min = 2, max = 150) String nome,
  @Size(min = 2, max = 255) String endereco,
  @Size(min = 2, max = 100) String cidade,
  @Size(min = 2, max = 2) String estado
) {
  public boolean semAlteracoes() {
    return (
      nome == null && endereco == null && cidade == null && estado == null
    );
  }
}
