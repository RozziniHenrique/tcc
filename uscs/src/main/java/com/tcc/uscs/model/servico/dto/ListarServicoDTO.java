package com.tcc.uscs.model.servico.dto;

import com.tcc.uscs.model.servico.Servico;
import java.math.BigDecimal;

public record ListarServicoDTO(Long id, String nome, BigDecimal valor) {
  public ListarServicoDTO(Servico s) {
    this(s.getId(), s.getNome(), s.getValor());
  }
}
