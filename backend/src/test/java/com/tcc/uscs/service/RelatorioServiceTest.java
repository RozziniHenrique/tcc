package com.tcc.uscs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.relatorio.dto.AgendamentosPorCursoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.AlunosPorCursoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.FaturamentoRelatorioDTO;
import com.tcc.uscs.repository.AgendamentoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

  @InjectMocks
  private RelatorioService relatorioService;

  @Mock
  private AgendamentoRepository agendamentoRepository;

  private final LocalDate inicio = LocalDate.of(2026, 1, 1);
  private final LocalDate fim = LocalDate.of(2026, 1, 31);

  @Test
  void deveriaGerarRelatorioCompleto() {
    var resumo = new FaturamentoRelatorioDTO(3L, new BigDecimal("450.00"));

    var alunos = List.of(new AlunosPorCursoRelatorioDTO(1L, "Estética", 5L));

    var agendamentos = List.of(
      new AgendamentosPorCursoRelatorioDTO(
        1L,
        "Estética",
        3L,
        new BigDecimal("450.00")
      )
    );

    when(
      agendamentoRepository.calcularFaturamentoPorPeriodo(
        inicio.atStartOfDay(),
        fim.atTime(LocalTime.MAX)
      )
    ).thenReturn(resumo);
    when(agendamentoRepository.contarAlunosPorCurso()).thenReturn(alunos);
    when(
      agendamentoRepository.calcularAgendamentosPorCurso(
        inicio.atStartOfDay(),
        fim.atTime(LocalTime.MAX)
      )
    ).thenReturn(agendamentos);

    var resultado = relatorioService.gerarRelatorioCompleto(inicio, fim);

    assertEquals(inicio, resultado.inicio());
    assertEquals(fim, resultado.fim());
    assertEquals(3L, resultado.resumo().totalAgendamentos());
    assertEquals(alunos, resultado.alunosPorCurso());
    assertEquals(agendamentos, resultado.agendamentosPorCurso());
  }

  @Test
  void deveriaRetornarFaturamentoZeradoQuandoNaoExistiremAgendamentos() {
    when(
      agendamentoRepository.calcularFaturamentoPorPeriodo(
        inicio.atStartOfDay(),
        fim.atTime(LocalTime.MAX)
      )
    ).thenReturn(null);

    var resultado = relatorioService.gerarRelatorioFaturamento(inicio, fim);

    assertEquals(0L, resultado.totalAgendamentos());
    assertEquals(BigDecimal.ZERO, resultado.faturamentoTotal());
  }

  @Test
  void deveriaRetornarAlunosPorCurso() {
    var alunos = List.of(new AlunosPorCursoRelatorioDTO(1L, "Estética", 5L));

    when(agendamentoRepository.contarAlunosPorCurso()).thenReturn(alunos);

    var resultado = relatorioService.gerarRelatorioAlunosPorCurso();

    assertEquals(alunos, resultado);
  }

  @Test
  void deveriaRetornarAgendamentosPorCurso() {
    var agendamentos = List.of(
      new AgendamentosPorCursoRelatorioDTO(
        1L,
        "Estética",
        2L,
        new BigDecimal("300.00")
      )
    );

    when(
      agendamentoRepository.calcularAgendamentosPorCurso(
        inicio.atStartOfDay(),
        fim.atTime(LocalTime.MAX)
      )
    ).thenReturn(agendamentos);

    var resultado = relatorioService.gerarRelatorioAgendamentosPorCurso(
      inicio,
      fim
    );

    assertEquals(agendamentos, resultado);
  }

  @Test
  void deveriaRecusarPeriodoInvertido() {
    var erro = assertThrows(ValidacaoException.class, () ->
      relatorioService.gerarRelatorioCompleto(fim, inicio)
    );

    assertEquals(
      "A data inicial não pode ser posterior à data final.",
      erro.getMessage()
    );
    verifyNoInteractions(agendamentoRepository);
  }

  @Test
  void deveriaRecusarDatasAusentes() {
    var erro = assertThrows(ValidacaoException.class, () ->
      relatorioService.gerarRelatorioCompleto(null, fim)
    );

    assertEquals(
      "As datas inicial e final são obrigatórias.",
      erro.getMessage()
    );
    verifyNoInteractions(agendamentoRepository);
  }

  @Test
  void deveriaRetornarRelatorioVazioQuandoNaoExistiremResultados() {
    when(
      agendamentoRepository.calcularFaturamentoPorPeriodo(
        inicio.atStartOfDay(),
        fim.atTime(LocalTime.MAX)
      )
    ).thenReturn(null);

    when(agendamentoRepository.contarAlunosPorCurso()).thenReturn(List.of());

    when(
      agendamentoRepository.calcularAgendamentosPorCurso(
        inicio.atStartOfDay(),
        fim.atTime(LocalTime.MAX)
      )
    ).thenReturn(List.of());

    var resultado = relatorioService.gerarRelatorioCompleto(inicio, fim);

    assertEquals(0L, resultado.resumo().totalAgendamentos());
    assertEquals(BigDecimal.ZERO, resultado.resumo().faturamentoTotal());
    assertTrue(resultado.alunosPorCurso().isEmpty());
    assertTrue(resultado.agendamentosPorCurso().isEmpty());
  }
}
