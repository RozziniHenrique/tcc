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
      f.getNome(),
      f.getEmail(),
      f.getTelefone(),
      f.getCpf(),
      f.getEndereco(),
      f.getFuncao(),
      f.getAtivo()
    );
  }
}
