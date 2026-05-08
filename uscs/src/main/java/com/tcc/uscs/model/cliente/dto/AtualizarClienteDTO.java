package com.tcc.uscs.model.cliente.dto;

import jakarta.validation.constraints.NotNull;

public record AtualizarClienteDTO(
  @NotNull Long id,
  String nome,
  String email,
  String telefone,
  String endereco,
  String observacoes
) {}
