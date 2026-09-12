package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CadastroUsuarioValidator {

  private final UsuarioRepository usuarioRepository;

  public void validarNovoUsuario(String cpf, String email) {
    if (usuarioRepository.existsByCpf(cpf)) {
      throw new ValidacaoException(
        "Já existe um usuário cadastrado com este CPF."
      );
    }

    if (usuarioRepository.existsByEmail(email)) {
      throw new ValidacaoException(
        "Já existe um usuário cadastrado com este e-mail."
      );
    }
  }
}
