package com.tcc.uscs.model.relatorio.dto;

import java.math.BigDecimal;

public record FaturamentoRelatorioDTO(
  Long totalAgendamentos,
  BigDecimal faturamentoTotal
) {}
