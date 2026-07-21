package com.tcc.uscs.controller;

import com.tcc.uscs.model.relatorio.dto.AlunosPorCursoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.FaturamentoRelatorioDTO;
import com.tcc.uscs.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("relatorios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FUNCIONARIO')")
public class RelatorioController {

  private final RelatorioService service;

  @GetMapping("/faturamento")
  @Operation(summary = "Obtém o faturamento total por período")
  public ResponseEntity<FaturamentoRelatorioDTO> getFaturamento(
    @RequestParam(required = false) @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE_TIME
    ) LocalDateTime inicio,
    @RequestParam(required = false) @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE_TIME
    ) LocalDateTime fim
  ) {
    return ResponseEntity.ok(service.obterRelatorioFaturamento(inicio, fim));
  }

  @GetMapping("/alunos-por-curso")
  @Operation(summary = "Obtém a quantidade de alunos matriculados por curso")
  public ResponseEntity<List<AlunosPorCursoRelatorioDTO>> getAlunosPorCurso() {
    return ResponseEntity.ok(service.obterAlunosPorCurso());
  }
}
