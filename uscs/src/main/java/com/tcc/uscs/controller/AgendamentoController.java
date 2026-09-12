package com.tcc.uscs.controller;

import com.tcc.uscs.model.agendamento.StatusAgendamento;
import com.tcc.uscs.model.agendamento.dto.*;
import com.tcc.uscs.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    @PageableDefault(
      size = 10,
      sort = "dataHora",
      direction = Sort.Direction.DESC
    ) Pageable paginacao,
    @RequestParam(required = false) StatusAgendamento status
  ) {
    return ResponseEntity.ok(service.listar(paginacao, status));
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
