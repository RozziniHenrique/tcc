package com.tcc.uscs.model.aluno.dto;

import com.tcc.uscs.model.aluno.Aluno;

public record ListarAlunoDTO(
  Long id,
  String nome,
  String email,
  String nomeCurso
) {
  public ListarAlunoDTO(Aluno aluno) {
    this(
      aluno.getId(),
      aluno.getNome(),
      aluno.getEmail(),
      aluno.getCurso().getNome()
    );
  }
}
