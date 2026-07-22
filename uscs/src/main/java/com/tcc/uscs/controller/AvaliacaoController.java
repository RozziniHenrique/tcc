package com.tcc.uscs.controller;

import com.tcc.uscs.model.avaliacao.dto.CadastrarAvaliacaoDTO;
import com.tcc.uscs.model.avaliacao.dto.DetalharAvaliacaoDTO;
import com.tcc.uscs.service.AvaliacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/avaliacoes")
@RequiredArgsConstructor
public class AvaliacaoController {

  private final AvaliacaoService service;

  @PostMapping
  public ResponseEntity<DetalharAvaliacaoDTO> avaliar(
    @RequestBody @Valid CadastrarAvaliacaoDTO dados,
    UriComponentsBuilder uriBuilder
  ) {
    var dto = service.avaliar(dados);
    var uri = uriBuilder
      .path("/avaliacoes/agendamento/{id}")
      .buildAndExpand(dto.idAgendamento())
      .toUri();
    return ResponseEntity.created(uri).body(dto);
  }

  @GetMapping("/agendamento/{id}")
  public ResponseEntity<DetalharAvaliacaoDTO> buscarPorAgendamento(
    @PathVariable Long id
  ) {
    var dto = service.buscarPorAgendamento(id);
    return ResponseEntity.ok(dto);
  }
}
