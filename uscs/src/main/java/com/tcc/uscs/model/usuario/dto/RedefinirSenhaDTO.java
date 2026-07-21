package com.tcc.uscs.model.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaDTO(
  @NotBlank String token,
  @NotBlank @Size(min = 6) String novaSenha
) {}
