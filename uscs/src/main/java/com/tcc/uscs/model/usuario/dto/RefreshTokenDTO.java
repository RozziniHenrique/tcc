package com.tcc.uscs.model.usuario.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenDTO(@NotBlank String refreshToken) {}
