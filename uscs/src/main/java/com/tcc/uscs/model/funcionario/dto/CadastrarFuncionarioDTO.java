package com.tcc.uscs.model.funcionario.dto;

import com.tcc.uscs.model.funcionario.Funcao;
import jakarta.validation.constraints.*;

public record CadastrarFuncionarioDTO(
  @NotBlank String nome,
  @NotBlank @Email String email,
  @NotBlank String senha,
  @NotBlank String telefone,
  @NotBlank String cpf,
  @NotBlank String endereco,
  @NotNull Funcao funcao
) {}
