package com.tcc.uscs.model.curso.dto;

import java.math.BigDecimal;

public record CadastrarCursoDTO(
  String nome,
  String descricao,
  String periodo,
  String duracao,
  String anoVigente,
  BigDecimal valor
) {}
