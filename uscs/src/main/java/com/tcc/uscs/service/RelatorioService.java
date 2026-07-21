package com.tcc.uscs.service;

import com.tcc.uscs.model.relatorio.dto.AlunosPorCursoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.FaturamentoRelatorioDTO;
import com.tcc.uscs.repository.AgendamentoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RelatorioService {

  private final AgendamentoRepository agendamentoRepository;

  public FaturamentoRelatorioDTO gerarRelatorioFaturamento(
    LocalDate inicio,
    LocalDate fim
  ) {
    LocalDateTime dataInicio = inicio.atStartOfDay();
    LocalDateTime dataFim = fim.atTime(LocalTime.MAX);

    FaturamentoRelatorioDTO dto =
      agendamentoRepository.calcularFaturamentoPorPeriodo(dataInicio, dataFim);

    if (dto == null || dto.totalAgendamentos() == null) {
      return new FaturamentoRelatorioDTO(0L, java.math.BigDecimal.ZERO);
    }

    return dto;
  }

  public List<AlunosPorCursoRelatorioDTO> gerarRelatorioAlunosPorCurso() {
    return agendamentoRepository.contarAlunosPorCurso();
  }
}
