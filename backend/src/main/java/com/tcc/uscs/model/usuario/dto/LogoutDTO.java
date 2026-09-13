package com.tcc.uscs.model.usuario.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutDTO(@NotBlank String refreshToken) {}
