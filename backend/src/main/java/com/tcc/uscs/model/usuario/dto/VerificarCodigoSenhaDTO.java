package com.tcc.uscs.model.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerificarCodigoSenhaDTO(
  @NotBlank @Email String email,
  @NotBlank
  @Pattern(regexp = "\\d{6}", message = "O código deve conter 6 dígitos")
  String codigo
) {}
