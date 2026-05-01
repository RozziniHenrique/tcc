package com.tcc.uscs.model.funcionario.dto;

import com.tcc.uscs.model.funcionario.Funcao;
import jakarta.validation.constraints.NotNull;

public record AtualizarFuncionarioDTO(
  @NotNull Long id,
  String nome,
  String email,
  String senha,
  String telefone,
  String endereco,
  Funcao funcao
) {}
