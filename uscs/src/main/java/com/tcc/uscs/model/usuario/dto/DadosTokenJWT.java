package com.tcc.uscs.model.usuario.dto;

public record DadosTokenJWT(
  String accessToken,
  String refreshToken,
  String tokenType,
  long expiresIn
) {}
