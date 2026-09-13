package com.tcc.uscs.model.relatorio.dto;

import java.math.BigDecimal;

public record AgendamentosPorCursoRelatorioDTO(
  Long idCurso,
  String nomeCurso,
  Long quantidadeAgendamentos,
  BigDecimal faturamentoTotal
) {}
