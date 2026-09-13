package com.tcc.uscs.model.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaDTO(
  @NotBlank @Email String email,
  @NotBlank
  @Pattern(regexp = "\\d{6}", message = "O código deve conter 6 dígitos")
  String codigo,
  @NotBlank
  @Size(min = 8, max = 72, message = "A senha deve ter pelo menos 8 caracteres")
  String novaSenha,
  @NotBlank
  @Size(
    min = 8,
    max = 72,
    message = "A confirmação deve ter pelo menos 8 caracteres"
  )
  String confirmacaoSenha
) {}
