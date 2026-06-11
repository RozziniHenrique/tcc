package com.tcc.uscs.model.funcionario.dto;

import com.tcc.uscs.model.funcionario.Funcao;

public record AtualizarFuncionarioDTO(
  String nome,
  String email,
  String telefone,
  String endereco,
  Funcao funcao
) {}
