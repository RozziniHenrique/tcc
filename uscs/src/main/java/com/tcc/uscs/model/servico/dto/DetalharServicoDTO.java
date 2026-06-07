package com.tcc.uscs.model.servico.dto;

import com.tcc.uscs.model.servico.Servico;
import java.math.BigDecimal;

public record DetalharServicoDTO(
  Long id,
  String nome,
  String descricao,
  BigDecimal valor,
  Boolean ativo
) {
  public DetalharServicoDTO(Servico s) {
    this(s.getId(), s.getNome(), s.getDescricao(), s.getValor(), s.getAtivo());
  }
}
