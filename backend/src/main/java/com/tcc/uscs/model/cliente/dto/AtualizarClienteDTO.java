package com.tcc.uscs.model.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AtualizarClienteDTO(
  @Size(min = 2, max = 255) String nome,
  @Email @Size(max = 100) String email,
  @Pattern(
    regexp = "\\d{10,11}",
    message = "O telefone deve conter 10 ou 11 dígitos"
  )
  String telefone,
  @Size(max = 255) String endereco,
  @Size(max = 1000) String observacoes
) {
  public boolean semAlteracoes() {
    return (
      nome == null &&
      email == null &&
      telefone == null &&
      endereco == null &&
      observacoes == null
    );
  }
}
