package com.tcc.uscs.model.aluno.dto;

import jakarta.validation.constraints.*;

public record CadastrarAlunoDTO(
  @NotBlank String nome,
  @NotBlank @Email String email,
  @NotBlank
  @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
  String senha,
  @NotBlank String telefone,
  @NotBlank @Pattern(regexp = "\\d{11}") String cpf,
  @NotBlank String endereco,
  @NotNull Long idCurso
) {}
