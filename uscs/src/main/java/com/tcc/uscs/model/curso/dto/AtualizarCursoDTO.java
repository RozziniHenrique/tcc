package com.tcc.uscs.model.curso.dto;

public record AtualizarCursoDTO(
  Long id,
  String nome,
  String descricao,
  String periodo,
  String duracao,
  String anoVigente,
  Boolean ativo
) {}
