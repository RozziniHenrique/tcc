package com.tcc.uscs.model.cliente.dto;

import com.tcc.uscs.model.cliente.Cliente;

public record DetalharClienteDTO(
  Long id,
  String nome,
  String email,
  String telefone,
  String cpf,
  Boolean ativo
) {
  public DetalharClienteDTO(Cliente cliente) {
    this(
      cliente.getId(),
      cliente.getUsuario().getNome(),
      cliente.getUsuario().getEmail(),
      cliente.getUsuario().getTelefone(),
      cliente.getUsuario().getCpf(),
      cliente.getUsuario().getAtivo()
    );
  }
}
