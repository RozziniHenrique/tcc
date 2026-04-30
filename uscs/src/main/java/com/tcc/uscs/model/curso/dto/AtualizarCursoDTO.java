package com.tcc.uscs.model.curso.dto;

import jakarta.validation.constraints.NotNull;

public record AtualizarCursoDTO(
  @NotNull Long id,
  String nome,
  String descricao,
  String periodo,
  String duracao,
  String anoVigente
) {}
