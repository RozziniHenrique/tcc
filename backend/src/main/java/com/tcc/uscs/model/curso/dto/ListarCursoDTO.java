package com.tcc.uscs.model.curso.dto;

import com.tcc.uscs.model.curso.Curso;
import java.math.BigDecimal;

public record ListarCursoDTO(
  Long id,
  String nome,
  String periodo,
  String duracao,
  String anoVigente,
  BigDecimal valor,
  Boolean ativo
) {
  public ListarCursoDTO(Curso curso) {
    this(
      curso.getId(),
      curso.getNome(),
      curso.getPeriodo(),
      curso.getDuracao(),
      curso.getAnoVigente(),
      curso.getValor(),
      curso.getAtivo()
    );
  }
}
