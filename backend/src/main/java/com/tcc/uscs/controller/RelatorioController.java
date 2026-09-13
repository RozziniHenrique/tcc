package com.tcc.uscs.controller;

import com.tcc.uscs.model.relatorio.dto.AgendamentosPorCursoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.AlunosPorCursoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.FaturamentoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.RelatorioCompletoDTO;
import com.tcc.uscs.service.RelatorioExportacaoService;
import com.tcc.uscs.service.RelatorioPdfService;
import com.tcc.uscs.service.RelatorioService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

  private final RelatorioService relatorioService;
  private final RelatorioExportacaoService relatorioExportacaoService;
  private final RelatorioPdfService relatorioPdfService;

  @GetMapping
  public ResponseEntity<RelatorioCompletoDTO> gerarRelatorio(
    @RequestParam @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE
    ) LocalDate inicio,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
  ) {
    return ResponseEntity.ok(
      relatorioService.gerarRelatorioCompleto(inicio, fim)
    );
  }

  @GetMapping("/faturamento")
  public ResponseEntity<FaturamentoRelatorioDTO> faturamento(
    @RequestParam @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE
    ) LocalDate inicio,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
  ) {
    return ResponseEntity.ok(
      relatorioService.gerarRelatorioFaturamento(inicio, fim)
    );
  }

  @GetMapping("/alunos-por-curso")
  public ResponseEntity<List<AlunosPorCursoRelatorioDTO>> alunosPorCurso() {
    return ResponseEntity.ok(relatorioService.gerarRelatorioAlunosPorCurso());
  }

  @GetMapping("/agendamentos-por-curso")
  public ResponseEntity<
    List<AgendamentosPorCursoRelatorioDTO>
  > agendamentosPorCurso(
    @RequestParam @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE
    ) LocalDate inicio,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
  ) {
    return ResponseEntity.ok(
      relatorioService.gerarRelatorioAgendamentosPorCurso(inicio, fim)
    );
  }

  @GetMapping(value = "/exportar/csv", produces = "text/csv")
  public ResponseEntity<byte[]> exportarCsv(
    @RequestParam @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE
    ) LocalDate inicio,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
  ) {
    var arquivo = relatorioExportacaoService.gerarCsv(inicio, fim);
    var nomeArquivo = "relatorio-stfer-" + inicio + "-" + fim + ".csv";

    return ResponseEntity.ok()
      .header(
        HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=\"" + nomeArquivo + "\""
      )
      .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
      .body(arquivo);
  }

  @GetMapping(
    value = "/exportar/pdf",
    produces = MediaType.APPLICATION_PDF_VALUE
  )
  public ResponseEntity<byte[]> exportarPdf(
    @RequestParam @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE
    ) LocalDate inicio,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
  ) {
    var arquivo = relatorioPdfService.gerarPdf(inicio, fim);
    var nomeArquivo = "relatorio-stfer-" + inicio + "-" + fim + ".pdf";

    return ResponseEntity.ok()
      .header(
        HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=\"" + nomeArquivo + "\""
      )
      .contentType(MediaType.APPLICATION_PDF)
      .body(arquivo);
  }
}
