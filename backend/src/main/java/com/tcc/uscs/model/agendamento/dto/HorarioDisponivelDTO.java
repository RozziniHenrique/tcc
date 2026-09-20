package com.tcc.uscs.model.agendamento.dto;

import java.time.LocalDateTime;

public record HorarioDisponivelDTO(
  LocalDateTime dataHora,
  long quantidadeAlunosDisponiveis
) {}
