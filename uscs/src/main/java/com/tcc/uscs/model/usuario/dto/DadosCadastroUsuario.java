package com.tcc.uscs.model.usuario.dto;

import com.tcc.uscs.model.usuario.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DadosCadastroUsuario(
  @NotBlank String nome,
  @NotBlank String cpf,
  @NotBlank @Email String email,
  @NotBlank
  @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
  String senha,
  @NotBlank String enderecoCompleto,
  @NotBlank String telefone,
  @NotNull TipoUsuario tipoUsuario
) {}
