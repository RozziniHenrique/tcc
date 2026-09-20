package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.relatorio.dto.AgendamentosPorCursoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.AlunosPorCursoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.DesempenhoAlunoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.FaturamentoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.RelatorioCompletoDTO;
import com.tcc.uscs.repository.AgendamentoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RelatorioService {

  private final AgendamentoRepository agendamentoRepository;

  @Transactional(readOnly = true)
  public RelatorioCompletoDTO gerarRelatorioCompleto(
    LocalDate inicio,
    LocalDate fim
  ) {
    validarPeriodo(inicio, fim);

    var resumo = gerarRelatorioFaturamento(inicio, fim);
    var alunosPorCurso = gerarRelatorioAlunosPorCurso();
    var agendamentosPorCurso = gerarRelatorioAgendamentosPorCurso(inicio, fim);

    return new RelatorioCompletoDTO(
      inicio,
      fim,
      resumo,
      alunosPorCurso,
      agendamentosPorCurso
    );
  }

  @Transactional(readOnly = true)
  public FaturamentoRelatorioDTO gerarRelatorioFaturamento(
    LocalDate inicio,
    LocalDate fim
  ) {
    validarPeriodo(inicio, fim);

    LocalDateTime dataInicio = inicio.atStartOfDay();
    LocalDateTime dataFim = fim.atTime(LocalTime.MAX);

    var resultado = agendamentoRepository.calcularFaturamentoPorPeriodo(
      dataInicio,
      dataFim
    );

    if (resultado == null || resultado.totalAgendamentos() == null) {
      return new FaturamentoRelatorioDTO(0L, BigDecimal.ZERO);
    }

    var faturamento =
      resultado.faturamentoTotal() == null
        ? BigDecimal.ZERO
        : resultado.faturamentoTotal();

    return new FaturamentoRelatorioDTO(
      resultado.totalAgendamentos(),
      faturamento
    );
  }

  @Transactional(readOnly = true)
  public List<AlunosPorCursoRelatorioDTO> gerarRelatorioAlunosPorCurso() {
    return agendamentoRepository.contarAlunosPorCurso();
  }

  @Transactional(readOnly = true)
  public List<
    AgendamentosPorCursoRelatorioDTO
  > gerarRelatorioAgendamentosPorCurso(LocalDate inicio, LocalDate fim) {
    validarPeriodo(inicio, fim);

    return agendamentoRepository.calcularAgendamentosPorCurso(
      inicio.atStartOfDay(),
      fim.atTime(LocalTime.MAX)
    );
  }

  @Transactional(readOnly = true)
  public List<DesempenhoAlunoRelatorioDTO> gerarDesempenhoAlunos(
    LocalDate inicio,
    LocalDate fim,
    Long idCurso,
    Long idAluno
  ) {
    validarPeriodo(inicio, fim);
    validarFiltroPositivo(idCurso, "curso");
    validarFiltroPositivo(idAluno, "aluno");

    return agendamentoRepository.calcularDesempenhoAlunos(
      inicio.atStartOfDay(),
      fim.atTime(LocalTime.MAX),
      idCurso,
      idAluno
    );
  }

  private void validarFiltroPositivo(Long id, String nomeFiltro) {
    if (id != null && id <= 0) {
      throw new ValidacaoException(
        "O identificador de " + nomeFiltro + " deve ser positivo."
      );
    }
  }

  private void validarPeriodo(LocalDate inicio, LocalDate fim) {
    if (inicio == null || fim == null) {
      throw new ValidacaoException(
        "As datas inicial e final são obrigatórias."
      );
    }

    if (inicio.isAfter(fim)) {
      throw new ValidacaoException(
        "A data inicial não pode ser posterior à data final."
      );
    }
  }
}
