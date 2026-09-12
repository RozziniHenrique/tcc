package com.tcc.uscs.model.relatorio.dto;

import java.time.LocalDate;
import java.util.List;

public record RelatorioCompletoDTO(
  LocalDate inicio,
  LocalDate fim,
  FaturamentoRelatorioDTO resumo,
  List<AlunosPorCursoRelatorioDTO> alunosPorCurso,
  List<AgendamentosPorCursoRelatorioDTO> agendamentosPorCurso
) {}
