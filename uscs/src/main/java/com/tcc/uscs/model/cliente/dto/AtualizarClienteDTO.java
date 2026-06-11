package com.tcc.uscs.model.cliente.dto;

public record AtualizarClienteDTO(
  String nome,
  String email,
  String telefone,
  String endereco,
  String observacoes
) {}
