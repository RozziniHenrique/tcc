package com.tcc.uscs.model.curso.dto;

public record CadastrarCursoDTO(
  String nome,
  String descricao,
  String periodo,
  String duracao,
  String anoVigente
) {}
