package com.tcc.uscs.model.funcionario.dto;

import com.tcc.uscs.model.funcionario.Funcao;
import com.tcc.uscs.model.funcionario.Funcionario;

public record DetalharFuncionarioDTO(
  Long id,
  String nome,
  String email,
  String telefone,
  String cpf,
  String endereco,
  Funcao funcao,
  Boolean ativo
) {
  public DetalharFuncionarioDTO(Funcionario f) {
    this(
      f.getId(),
      f.getUsuario().getNome(),
      f.getUsuario().getEmail(),
      f.getUsuario().getTelefone(),
      f.getUsuario().getCpf(),
      f.getUsuario().getEnderecoCompleto(),
      f.getFuncao(),
      f.getUsuario().getAtivo()
    );
  }
}
