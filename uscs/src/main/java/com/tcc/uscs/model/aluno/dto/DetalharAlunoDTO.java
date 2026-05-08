package com.tcc.uscs.model.aluno.dto;

import com.tcc.uscs.model.aluno.Aluno;
import com.tcc.uscs.model.curso.dto.DetalharCursoDTO;

public record DetalharAlunoDTO(
  Long id,
  String nome,
  String email,
  String telefone,
  String cpf,
  String endereco,
  DetalharCursoDTO curso
) {
  public DetalharAlunoDTO(Aluno aluno) {
    this(
      aluno.getId(),
      aluno.getUsuario().getNome(),
      aluno.getUsuario().getEmail(),
      aluno.getUsuario().getTelefone(),
      aluno.getUsuario().getCpf(),
      aluno.getUsuario().getEnderecoCompleto(),
      aluno.getCurso() != null ? new DetalharCursoDTO(aluno.getCurso()) : null
    );
  }
}
