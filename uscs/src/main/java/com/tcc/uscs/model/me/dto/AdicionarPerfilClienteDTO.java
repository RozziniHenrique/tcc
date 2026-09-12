package com.tcc.uscs.model.me.dto;

import jakarta.validation.constraints.Size;

public record AdicionarPerfilClienteDTO(@Size(max = 1000) String observacoes) {}
