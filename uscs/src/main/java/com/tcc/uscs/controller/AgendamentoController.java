package com.tcc.uscs.controller;

import com.tcc.uscs.model.agendamento.dto.CadastrarAgendamentoDTO;
import com.tcc.uscs.service.AgendamentoService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("agendamentos")
public class AgendamentoController {

  @Autowired
  private AgendamentoService service;

  @PostMapping
  @Transactional
  public ResponseEntity agendar(
    @RequestBody @Valid CadastrarAgendamentoDTO dados
  ) {
    var detalhamento = service.agendar(dados);
    return ResponseEntity.ok(detalhamento);
  }

  @DeleteMapping("/{id}")
  @Transactional
  public ResponseEntity cancelar(
    @PathVariable Long id,
    @RequestBody String justificativa
  ) {
    service.cancelar(id, justificativa);
    return ResponseEntity.noContent().build();
  }
}
