package com.tcc.uscs.controller;

import com.tcc.uscs.model.agendamento.StatusAgendamento;
import com.tcc.uscs.model.agendamento.dto.*;
import com.tcc.uscs.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@RestController
@RequestMapping("agendamentos")
public class AgendamentoController {

  private final AgendamentoService service;

  @PostMapping
  @Operation(summary = "Cadastra agendamento")
  public ResponseEntity<DetalharAgendamentoDTO> agendar(
    @RequestBody @Valid CadastrarAgendamentoDTO dados,
    UriComponentsBuilder uriBuilder
  ) {
    var detalhamento = service.agendar(dados);
    var uri = uriBuilder
      .path("/agendamentos/{id}")
      .buildAndExpand(detalhamento.id())
      .toUri();
    return ResponseEntity.created(uri).body(detalhamento);
  }

  @GetMapping
  @Operation(summary = "Lista agendamentos")
  public ResponseEntity<Page<ListarAgendamentoDTO>> listar(
    @ParameterObject @PageableDefault(
      size = 10,
      sort = "dataHora",
      direction = Sort.Direction.DESC
    ) Pageable paginacao,
    @RequestParam(required = false) StatusAgendamento status,
    @RequestParam(required = false) @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE
    ) LocalDate inicio,
    @RequestParam(required = false) @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE
    ) LocalDate fim,
    @RequestParam(required = false) Long idCurso,
    @RequestParam(required = false) Long idAluno,
    @RequestParam(required = false) Long idCliente,
    @RequestParam(required = false) Long idUnidade
  ) {
    var filtros = new FiltroAgendamentoDTO(
      status,
      inicio,
      fim,
      idCurso,
      idAluno,
      idCliente,
      idUnidade
    );

    return ResponseEntity.ok(service.listar(paginacao, filtros));
  }

  @GetMapping("/disponibilidade")
  @Operation(summary = "Consulta horários disponíveis para um curso e data")
  public ResponseEntity<List<HorarioDisponivelDTO>> consultarDisponibilidade(
    @RequestParam Long idCurso,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
  ) {
    return ResponseEntity.ok(service.consultarDisponibilidade(idCurso, data));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Detalhar agendamento")
  public ResponseEntity<DetalharAgendamentoDTO> detalhar(
    @PathVariable Long id
  ) {
    return ResponseEntity.ok(service.detalhar(id));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualiza ou reagenda um agendamento")
  public ResponseEntity<DetalharAgendamentoDTO> atualizar(
    @PathVariable Long id,
    @RequestBody @Valid AtualizarAgendamentoDTO dados
  ) {
    return ResponseEntity.ok(service.atualizar(id, dados));
  }

  @PatchMapping("/{id}/concluir")
  @Operation(summary = "Conclui um agendamento realizado")
  public ResponseEntity<Void> concluir(@PathVariable Long id) {
    service.concluir(id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Cancelar agendamento")
  public ResponseEntity<Void> cancelar(
    @PathVariable Long id,
    @RequestBody @Valid CancelamentoRequestDTO dto
  ) {
    service.cancelar(id, dto.justificativa());
    return ResponseEntity.noContent().build();
  }
}
