package com.tcc.uscs.controller;

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
  @Operation(summary = "Listar agendamento")
  public ResponseEntity<Page<ListarAgendamentoDTO>> listar(
    @PageableDefault(
      size = 10,
      sort = "dataHora",
      direction = Sort.Direction.DESC
    ) Pageable paginacao
  ) {
    return ResponseEntity.ok(service.listar(paginacao));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Detalhar agendamento")
  public ResponseEntity<DetalharAgendamentoDTO> detalhar(
    @PathVariable Long id
  ) {
    return ResponseEntity.ok(service.detalhar(id));
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
