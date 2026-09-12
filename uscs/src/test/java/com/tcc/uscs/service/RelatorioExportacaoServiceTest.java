package com.tcc.uscs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.tcc.uscs.model.relatorio.dto.AgendamentosPorCursoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.AlunosPorCursoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.FaturamentoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.RelatorioCompletoDTO;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RelatorioExportacaoServiceTest {

  @InjectMocks
  private RelatorioExportacaoService exportacaoService;

  @Mock
  private RelatorioService relatorioService;

  @Test
  void deveriaGerarArquivoCsv() {
    var inicio = LocalDate.of(2026, 1, 1);
    var fim = LocalDate.of(2026, 1, 31);

    var relatorio = new RelatorioCompletoDTO(
      inicio,
      fim,
      new FaturamentoRelatorioDTO(2L, new BigDecimal("300.00")),
      List.of(new AlunosPorCursoRelatorioDTO(1L, "Estética", 5L)),
      List.of(
        new AgendamentosPorCursoRelatorioDTO(
          1L,
          "Estética",
          2L,
          new BigDecimal("300.00")
        )
      )
    );

    when(relatorioService.gerarRelatorioCompleto(inicio, fim)).thenReturn(
      relatorio
    );

    var arquivo = exportacaoService.gerarCsv(inicio, fim);
    var conteudo = new String(arquivo, StandardCharsets.UTF_8);

    assertTrue(arquivo.length > 0);
    assertTrue(conteudo.startsWith("\uFEFF"));
    assertTrue(conteudo.contains("RELATÓRIO STFER"));
    assertTrue(conteudo.contains("Total de agendamentos;2"));
    assertTrue(conteudo.contains("\"Estética\";5"));
    assertTrue(conteudo.contains("\"Estética\";2;300.00"));
  }

  @Test
  void deveriaProtegerCelulaContraFormulaDePlanilha() {
    var inicio = LocalDate.of(2026, 1, 1);
    var fim = LocalDate.of(2026, 1, 31);

    var relatorio = new RelatorioCompletoDTO(
      inicio,
      fim,
      new FaturamentoRelatorioDTO(0L, BigDecimal.ZERO),
      List.of(new AlunosPorCursoRelatorioDTO(1L, "=FORMULA", 1L)),
      List.of()
    );

    when(relatorioService.gerarRelatorioCompleto(inicio, fim)).thenReturn(
      relatorio
    );

    var conteudo = new String(
      exportacaoService.gerarCsv(inicio, fim),
      StandardCharsets.UTF_8
    );

    assertTrue(conteudo.contains("\"'=FORMULA\""));
  }
}
