package com.tcc.uscs.model.avaliacao.dto;

import java.time.LocalDateTime;

public record AvaliacaoPendenteDTO(
  Long idAgendamento,
  Long idAluno,
  String nomeAluno,
  Long idCurso,
  String nomeCurso,
  LocalDateTime dataHora
) {}
