package com.tcc.uscs.infra.exception;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErroApiDTO(
  OffsetDateTime timestamp,
  int status,
  String error,
  String message,
  Map<String, String> fields
) {
  public static ErroApiDTO of(int status, String error, String message) {
    return new ErroApiDTO(
      OffsetDateTime.now(),
      status,
      error,
      message,
      Map.of()
    );
  }

  public static ErroApiDTO validation(int status, Map<String, String> fields) {
    return new ErroApiDTO(
      OffsetDateTime.now(),
      status,
      "VALIDATION_ERROR",
      "Um ou mais campos são inválidos.",
      fields
    );
  }
}
