package com.tcc.uscs.model.cliente.dto;

import jakarta.validation.constraints.*;

public record CadastrarClienteDTO(
  @NotBlank @Size(min = 2, max = 255) String nome,

  @NotBlank @Email @Size(max = 100) String email,

  @NotBlank
  @Size(
    min = 8,
    max = 72,
    message = "A senha deve conter entre 8 e 72 caracteres"
  )
  String senha,

  @NotBlank
  @Pattern(
    regexp = "\\d{10,11}",
    message = "O telefone deve conter 10 ou 11 dígitos"
  )
  String telefone,

  @NotBlank
  @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 dígitos")
  String cpf,

  @NotBlank @Size(max = 255) String endereco,

  @Size(max = 1000) String observacoes
) {}
