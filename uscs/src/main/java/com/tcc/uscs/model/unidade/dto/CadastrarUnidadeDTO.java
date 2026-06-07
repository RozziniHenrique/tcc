package com.tcc.uscs.model.unidade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastrarUnidadeDTO(
  @NotBlank String nome,
  @NotBlank String endereco,
  @NotBlank String cidade,
  @NotBlank @Size(min = 2, max = 2) String estado
) {}
