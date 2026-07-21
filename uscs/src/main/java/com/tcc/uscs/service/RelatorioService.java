package com.tcc.uscs.service;

import com.tcc.uscs.model.relatorio.dto.AlunosPorCursoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.FaturamentoRelatorioDTO;
import com.tcc.uscs.repository.AgendamentoRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RelatorioService {

  private final AgendamentoRepository repository;

  public FaturamentoRelatorioDTO obterRelatorioFaturamento(
    LocalDateTime inicio,
    LocalDateTime fim
  ) {
    var dataInicio = (inicio != null)
      ? inicio
      : LocalDateTime.now().minusDays(30);
    var dataFim = (fim != null) ? fim : LocalDateTime.now();

    var resultado = repository.calcularFaturamentoPorPeriodo(
      dataInicio,
      dataFim
    );

    if (resultado.faturamentoTotal() == null) {
      return new FaturamentoRelatorioDTO(0L, java.math.BigDecimal.ZERO);
    }

    return resultado;
  }

  public List<AlunosPorCursoRelatorioDTO> obterAlunosPorCurso() {
    return repository.contarAlunosPorCurso();
  }
}
