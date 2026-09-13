package com.tcc.uscs.model.me.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AdicionarPerfilAlunoDTO(@NotNull @Positive Long idCurso) {}
