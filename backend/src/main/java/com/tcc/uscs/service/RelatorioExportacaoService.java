package com.tcc.uscs.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RelatorioExportacaoService {

  private final RelatorioService relatorioService;

  public byte[] gerarCsv(LocalDate inicio, LocalDate fim) {
    var relatorio = relatorioService.gerarRelatorioCompleto(inicio, fim);
    var csv = new StringBuilder();

    csv.append('\uFEFF');
    csv.append("RELATÓRIO STFER\n");
    csv
      .append("Período;")
      .append(relatorio.inicio())
      .append(";")
      .append(relatorio.fim())
      .append("\n");

    csv
      .append("Total de agendamentos;")
      .append(relatorio.resumo().totalAgendamentos())
      .append("\n");

    csv
      .append("Faturamento total;")
      .append(relatorio.resumo().faturamentoTotal().toPlainString())
      .append("\n\n");

    csv.append("ALUNOS POR CURSO\n");
    csv.append("ID do curso;Curso;Quantidade de alunos\n");

    for (var item : relatorio.alunosPorCurso()) {
      csv
        .append(item.idCurso())
        .append(";")
        .append(escapar(item.nomeCurso()))
        .append(";")
        .append(item.quantidadeAlunos())
        .append("\n");
    }

    csv.append("\nAGENDAMENTOS E FATURAMENTO POR CURSO\n");
    csv.append("ID do curso;Curso;Agendamentos;Faturamento\n");

    for (var item : relatorio.agendamentosPorCurso()) {
      csv
        .append(item.idCurso())
        .append(";")
        .append(escapar(item.nomeCurso()))
        .append(";")
        .append(item.quantidadeAgendamentos())
        .append(";")
        .append(item.faturamentoTotal().toPlainString())
        .append("\n");
    }

    return csv.toString().getBytes(StandardCharsets.UTF_8);
  }

  public byte[] gerarXlsx(LocalDate inicio, LocalDate fim) {
    var relatorio = relatorioService.gerarRelatorioCompleto(inicio, fim);

    try (
      Workbook workbook = new XSSFWorkbook();
      var arquivo = new ByteArrayOutputStream()
    ) {
      var cabecalho = criarEstiloCabecalho(workbook);
      var monetario = criarEstiloMonetario(workbook);

      var resumo = workbook.createSheet("Resumo");
      adicionarLinha(resumo, 0, "RELATÓRIO STFER");
      adicionarLinha(
        resumo,
        2,
        "Período",
        relatorio.inicio() + " a " + relatorio.fim()
      );
      adicionarLinha(
        resumo,
        3,
        "Total de agendamentos",
        relatorio.resumo().totalAgendamentos()
      );

      var linhaFaturamento = resumo.createRow(4);
      linhaFaturamento.createCell(0).setCellValue("Faturamento total");
      var celulaFaturamento = linhaFaturamento.createCell(1);
      celulaFaturamento.setCellValue(
        relatorio.resumo().faturamentoTotal().doubleValue()
      );
      celulaFaturamento.setCellStyle(monetario);
      ajustarColunas(resumo, 2);

      var alunos = workbook.createSheet("Alunos por curso");
      adicionarCabecalho(
        alunos,
        cabecalho,
        "ID do curso",
        "Curso",
        "Quantidade de alunos"
      );

      int linha = 1;
      for (var item : relatorio.alunosPorCurso()) {
        var registro = alunos.createRow(linha++);
        registro.createCell(0).setCellValue(item.idCurso());
        registro.createCell(1).setCellValue(item.nomeCurso());
        registro.createCell(2).setCellValue(item.quantidadeAlunos());
      }
      ajustarColunas(alunos, 3);

      var agendamentos = workbook.createSheet("Agendamentos por curso");
      adicionarCabecalho(
        agendamentos,
        cabecalho,
        "ID do curso",
        "Curso",
        "Agendamentos",
        "Faturamento"
      );

      linha = 1;
      for (var item : relatorio.agendamentosPorCurso()) {
        var registro = agendamentos.createRow(linha++);
        registro.createCell(0).setCellValue(item.idCurso());
        registro.createCell(1).setCellValue(item.nomeCurso());
        registro.createCell(2).setCellValue(item.quantidadeAgendamentos());
        var valor = registro.createCell(3);
        valor.setCellValue(item.faturamentoTotal().doubleValue());
        valor.setCellStyle(monetario);
      }
      ajustarColunas(agendamentos, 4);

      workbook.write(arquivo);
      return arquivo.toByteArray();
    } catch (IOException exception) {
      throw new IllegalStateException(
        "Não foi possível gerar o relatório em Excel.",
        exception
      );
    }
  }

  private CellStyle criarEstiloCabecalho(Workbook workbook) {
    var fonte = workbook.createFont();
    fonte.setBold(true);
    fonte.setColor(IndexedColors.WHITE.getIndex());

    var estilo = workbook.createCellStyle();
    estilo.setFont(fonte);
    estilo.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
    estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    return estilo;
  }

  private CellStyle criarEstiloMonetario(Workbook workbook) {
    var estilo = workbook.createCellStyle();
    estilo.setDataFormat(
      workbook.createDataFormat().getFormat("R$ #,##0.00")
    );
    return estilo;
  }

  private void adicionarCabecalho(
    Sheet planilha,
    CellStyle estilo,
    String... titulos
  ) {
    var linha = planilha.createRow(0);
    for (int coluna = 0; coluna < titulos.length; coluna++) {
      var celula = linha.createCell(coluna);
      celula.setCellValue(titulos[coluna]);
      celula.setCellStyle(estilo);
    }
  }

  private void adicionarLinha(
    Sheet planilha,
    int numeroLinha,
    String titulo
  ) {
    planilha.createRow(numeroLinha).createCell(0).setCellValue(titulo);
  }

  private void adicionarLinha(
    Sheet planilha,
    int numeroLinha,
    String titulo,
    String valor
  ) {
    var linha = planilha.createRow(numeroLinha);
    linha.createCell(0).setCellValue(titulo);
    linha.createCell(1).setCellValue(valor);
  }

  private void adicionarLinha(
    Sheet planilha,
    int numeroLinha,
    String titulo,
    long valor
  ) {
    var linha = planilha.createRow(numeroLinha);
    linha.createCell(0).setCellValue(titulo);
    linha.createCell(1).setCellValue(valor);
  }

  private void ajustarColunas(Sheet planilha, int quantidade) {
    for (int coluna = 0; coluna < quantidade; coluna++) {
      planilha.autoSizeColumn(coluna);
    }
  }

  private String escapar(String valor) {
    if (valor == null) {
      return "";
    }

    var texto = valor.replace("\"", "\"\"");

    if (
      texto.startsWith("=") ||
      texto.startsWith("+") ||
      texto.startsWith("-") ||
      texto.startsWith("@")
    ) {
      texto = "'" + texto;
    }

    return "\"" + texto + "\"";
  }
}
