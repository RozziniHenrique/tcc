package com.tcc.uscs.controller;

import com.tcc.uscs.model.agendamento.dto.CadastrarAgendamentoDTO;
import com.tcc.uscs.service.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@RestController
@RequestMapping("agendamentos")
public class AgendamentoController {

  private final AgendamentoService service;

  @PostMapping
  public ResponseEntity agendar(
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

  @DeleteMapping("/{id}")
  public ResponseEntity cancelar(
    @PathVariable Long id,
    @RequestBody String justificativa
  ) {
    service.cancelar(id, justificativa);
    return ResponseEntity.noContent().build();
  }
}
