package com.tcc.uscs.model.cliente.dto;

import jakarta.validation.constraints.*;

public record CadastrarClienteDTO(
  @NotBlank String nome,
  @NotBlank @Email String email,
  @NotBlank @Size(min = 6) String senha,
  @NotBlank String telefone,
  @NotBlank @Pattern(regexp = "\\d{11}") String cpf
) {}
