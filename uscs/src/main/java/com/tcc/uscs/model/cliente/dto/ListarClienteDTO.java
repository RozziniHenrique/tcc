package com.tcc.uscs.model.cliente.dto;

import com.tcc.uscs.model.cliente.Cliente;

public record ListarClienteDTO(
  Long id,
  String nome,
  String email,
  String telefone
) {
  public ListarClienteDTO(Cliente cliente) {
    this(
      cliente.getId(),
      cliente.getNome(),
      cliente.getEmail(),
      cliente.getTelefone()
    );
  }
}
