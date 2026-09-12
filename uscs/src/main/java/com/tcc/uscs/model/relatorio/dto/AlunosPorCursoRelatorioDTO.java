package com.tcc.uscs.model.relatorio.dto;

public record AlunosPorCursoRelatorioDTO(
  Long idCurso,
  String nomeCurso,
  Long quantidadeAlunos
) {}
