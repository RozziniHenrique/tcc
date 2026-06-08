package com.tcc.uscs.model.servico.dto;

import java.math.BigDecimal;

public record AtualizarServicoDTO(
  String nome,
  String descricao,
  BigDecimal valor
) {}
