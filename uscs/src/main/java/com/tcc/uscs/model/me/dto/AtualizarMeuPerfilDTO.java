package com.tcc.uscs.model.me.dto;

import jakarta.validation.constraints.Size;

public record AtualizarMeuPerfilDTO(
  @Size(max = 255) String nome,
  @Size(max = 20) String telefone,
  @Size(max = 255) String enderecoCompleto
) {}
