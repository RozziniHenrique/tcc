package com.tcc.uscs.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
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
