package com.tcc.uscs.model.cliente.dto;

import com.tcc.uscs.model.cliente.Cliente;

public record DetalharClienteDTO(
  Long id,
  String nome,
  String email,
  String telefone,
  String cpf
) {
  public DetalharClienteDTO(Cliente cliente) {
    this(
      cliente.getId(),
      cliente.getNome(),
      cliente.getEmail(),
      cliente.getTelefone(),
      cliente.getCpf()
    );
  }
}
