package com.tcc.uscs.model.curso.dto;

import com.tcc.uscs.model.curso.Curso;

public record ListarCursoDTO(
  Long id,
  String nome,
  String descricao,
  String periodo,
  String duracao,
  String anoVigente,
  Boolean ativo
) {
  public ListarCursoDTO(Curso curso) {
    this(
      curso.getId(),
      curso.getNome(),
      curso.getDescricao(),
      curso.getPeriodo(),
      curso.getDuracao(),
      curso.getAnoVigente(),
      curso.getAtivo()
    );
  }
}
