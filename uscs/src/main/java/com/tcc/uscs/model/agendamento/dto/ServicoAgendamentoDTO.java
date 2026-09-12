package com.tcc.uscs.model.agendamento.dto;

import com.tcc.uscs.model.servico.Servico;
import java.math.BigDecimal;

public record ServicoAgendamentoDTO(Long id, String nome, BigDecimal valor) {
  public ServicoAgendamentoDTO(Servico servico) {
    this(servico.getId(), servico.getNome(), servico.getValor());
  }
}
