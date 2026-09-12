package com.tcc.uscs.model.me.dto;

import com.tcc.uscs.model.usuario.Usuario;
import java.util.Set;
import java.util.stream.Collectors;

public record MeuPerfilDTO(
  Long id,
  String nome,
  String cpf,
  String email,
  String telefone,
  String enderecoCompleto,
  Set<String> perfis,
  String funcao
) {
  public MeuPerfilDTO(Usuario usuario) {
    this(
      usuario.getId(),
      usuario.getNome(),
      usuario.getCpf(),
      usuario.getEmail(),
      usuario.getTelefone(),
      usuario.getEnderecoCompleto(),
      usuario.getPerfis().stream().map(Enum::name).collect(Collectors.toSet()),
      usuario.getFuncionario() == null
        ? null
        : usuario.getFuncionario().getFuncao().name()
    );
  }
}
