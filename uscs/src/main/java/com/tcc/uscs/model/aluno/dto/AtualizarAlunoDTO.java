package com.tcc.uscs.model.aluno.dto;

public record AtualizarAlunoDTO(
  String nome,
  String email,
  String telefone,
  String endereco
) {}
