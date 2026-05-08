package com.tcc.uscs.model.aluno.dto;

import jakarta.validation.constraints.NotNull;

public record AtualizarAlunoDTO(
  @NotNull Long id,
  String nome,
  String email,
  String telefone,
  String endereco
) {}
