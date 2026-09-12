package com.tcc.uscs.model.funcionario.dto;

import com.tcc.uscs.model.funcionario.Funcao;
import jakarta.validation.constraints.NotNull;

public record AdicionarPerfilFuncionarioDTO(@NotNull Funcao funcao) {}
