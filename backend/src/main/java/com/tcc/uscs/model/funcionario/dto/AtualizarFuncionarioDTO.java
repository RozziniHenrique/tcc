package com.tcc.uscs.model.funcionario.dto;

import com.tcc.uscs.model.funcionario.Funcao;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AtualizarFuncionarioDTO(
  @Size(min = 2, max = 255) String nome,
  @Email @Size(max = 100) String email,
  @Pattern(
    regexp = "\\d{10,11}",
    message = "O telefone deve conter 10 ou 11 dígitos"
  )
  String telefone,
  @Size(max = 255) String endereco,
  Funcao funcao
) {
  public boolean semAlteracoes() {
    return (
      nome == null &&
      email == null &&
      telefone == null &&
      endereco == null &&
      funcao == null
    );
  }
}
