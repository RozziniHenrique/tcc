package com.tcc.uscs.model.me.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AtualizarMeuPerfilDTO(
  @Size(min = 2, max = 255) String nome,

  @Pattern(
    regexp = "\\d{10,11}",
    message = "O telefone deve conter 10 ou 11 dígitos"
  )
  String telefone,

  @Size(min = 2, max = 255) String enderecoCompleto
) {
  public boolean semAlteracoes() {
    return (nome == null && telefone == null && enderecoCompleto == null);
  }
}
