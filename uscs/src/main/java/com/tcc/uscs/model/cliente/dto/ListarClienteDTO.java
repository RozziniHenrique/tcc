package com.tcc.uscs.model.cliente.dto;

import com.tcc.uscs.model.cliente.Cliente;

public record ListarClienteDTO(
  Long id,
  String nome,
  String email,
  String telefone,
  Boolean ativo
) {
  public ListarClienteDTO(Cliente cliente) {
    this(
      cliente.getId(),
      cliente.getUsuario().getNome(),
      cliente.getUsuario().getEmail(),
      cliente.getUsuario().getTelefone(),
      cliente.getUsuario().getAtivo()
    );
  }
}
