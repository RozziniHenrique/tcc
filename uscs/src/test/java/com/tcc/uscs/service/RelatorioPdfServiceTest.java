package com.tcc.uscs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.tcc.uscs.model.relatorio.dto.AgendamentosPorCursoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.AlunosPorCursoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.FaturamentoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.RelatorioCompletoDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RelatorioPdfServiceTest {

  @InjectMocks
  private RelatorioPdfService pdfService;

  @Mock
  private RelatorioService relatorioService;

  private final LocalDate inicio = LocalDate.of(2026, 1, 1);
  private final LocalDate fim = LocalDate.of(2026, 1, 31);

  @Test
  void deveriaGerarPdfValido() throws Exception {
    when(relatorioService.gerarRelatorioCompleto(inicio, fim)).thenReturn(
      criarRelatorio(
        List.of(new AlunosPorCursoRelatorioDTO(1L, "Estética", 5L))
      )
    );

    var arquivo = pdfService.gerarPdf(inicio, fim);

    assertTrue(arquivo.length > 0);
    assertEquals("%PDF-", new String(arquivo, 0, 5));

    try (PDDocument documento = Loader.loadPDF(arquivo)) {
      assertEquals(1, documento.getNumberOfPages());

      var texto = new PDFTextStripper().getText(documento);

      assertTrue(texto.contains("RELATÓRIO STFER"));
      assertTrue(texto.contains("Total de agendamentos: 2"));
      assertTrue(texto.contains("Estética"));
    }
  }

  @Test
  void deveriaCriarMaisDeUmaPaginaQuandoRelatorioForGrande() throws Exception {
    var alunos = IntStream.rangeClosed(1, 80)
      .mapToObj(numero ->
        new AlunosPorCursoRelatorioDTO(
          (long) numero,
          "Curso " + numero,
          (long) numero
        )
      )
      .toList();

    when(relatorioService.gerarRelatorioCompleto(inicio, fim)).thenReturn(
      criarRelatorio(alunos)
    );

    var arquivo = pdfService.gerarPdf(inicio, fim);

    try (PDDocument documento = Loader.loadPDF(arquivo)) {
      assertTrue(documento.getNumberOfPages() > 1);
    }
  }

  private RelatorioCompletoDTO criarRelatorio(
    List<AlunosPorCursoRelatorioDTO> alunos
  ) {
    return new RelatorioCompletoDTO(
      inicio,
      fim,
      new FaturamentoRelatorioDTO(2L, new BigDecimal("300.00")),
      alunos,
      List.of(
        new AgendamentosPorCursoRelatorioDTO(
          1L,
          "Estética",
          2L,
          new BigDecimal("300.00")
        )
      )
    );
  }
}
