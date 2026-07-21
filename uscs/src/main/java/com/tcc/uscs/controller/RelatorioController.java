package com.tcc.uscs.controller;

import com.tcc.uscs.model.relatorio.dto.AlunosPorCursoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.FaturamentoRelatorioDTO;
import com.tcc.uscs.service.RelatorioService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

  private final RelatorioService relatorioService;

  @GetMapping("/faturamento")
  public ResponseEntity<FaturamentoRelatorioDTO> faturamento(
    @RequestParam @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE
    ) LocalDate inicio,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
  ) {
    var relatorio = relatorioService.gerarRelatorioFaturamento(inicio, fim);
    return ResponseEntity.ok(relatorio);
  }

  @GetMapping("/alunos-por-curso")
  public ResponseEntity<List<AlunosPorCursoRelatorioDTO>> alunosPorCurso() {
    var relatorio = relatorioService.gerarRelatorioAlunosPorCurso();
    return ResponseEntity.ok(relatorio);
  }
}
