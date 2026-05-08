package com.tcc.uscs.model.funcionario.dto;

import com.tcc.uscs.model.funcionario.Funcao;
import com.tcc.uscs.model.funcionario.Funcionario;

public record ListarFuncionarioDTO(
  Long id,
  String nome,
  String email,
  Funcao funcao
) {
  public ListarFuncionarioDTO(Funcionario f) {
    this(
      f.getId(),
      f.getUsuario().getNome(),
      f.getUsuario().getEmail(),
      f.getFuncao()
    );
  }
}
