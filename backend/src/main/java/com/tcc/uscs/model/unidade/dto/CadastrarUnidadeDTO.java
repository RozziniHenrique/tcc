package com.tcc.uscs.model.unidade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastrarUnidadeDTO(
  @NotBlank @Size(max = 150) String nome,
  @NotBlank @Size(max = 255) String endereco,
  @NotBlank @Size(max = 100) String cidade,
  @NotBlank @Size(min = 2, max = 2) String estado
) {}
