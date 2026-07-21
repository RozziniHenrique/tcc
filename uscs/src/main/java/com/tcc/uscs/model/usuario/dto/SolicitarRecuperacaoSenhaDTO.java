package com.tcc.uscs.model.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SolicitarRecuperacaoSenhaDTO(@NotBlank @Email String email) {}
