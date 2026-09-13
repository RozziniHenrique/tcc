package com.tcc.uscs.model.avaliacao.dto;

import com.tcc.uscs.model.avaliacao.Avaliacao;
import java.time.LocalDateTime;

public record DetalharAvaliacaoDTO(
  Long id,
  Long idAgendamento,
  Integer nota,
  String comentario,
  LocalDateTime dataAvaliacao
) {
  public DetalharAvaliacaoDTO(Avaliacao avaliacao) {
    this(
      avaliacao.getId(),
      avaliacao.getAgendamento().getId(),
      avaliacao.getNota(),
      avaliacao.getComentario(),
      avaliacao.getDataAvaliacao()
    );
  }
}
