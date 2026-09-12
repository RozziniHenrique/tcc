package com.tcc.uscs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CadastroUsuarioValidatorTest {

  @Mock
  private UsuarioRepository usuarioRepository;

  @InjectMocks
  private CadastroUsuarioValidator validator;

  @Test
  void deveRejeitarCpfJaCadastrado() {
    when(usuarioRepository.existsByCpf("12345678901")).thenReturn(true);

    var erro = assertThrows(ValidacaoException.class, () ->
      validator.validarNovoUsuario("12345678901", "usuario@email.com")
    );

    assertEquals(
      "Já existe um usuário cadastrado com este CPF.",
      erro.getMessage()
    );
    verify(usuarioRepository, never()).existsByEmail(anyString());
  }

  @Test
  void deveRejeitarEmailJaCadastrado() {
    when(usuarioRepository.existsByCpf("12345678901")).thenReturn(false);
    when(usuarioRepository.existsByEmail("usuario@email.com")).thenReturn(true);

    var erro = assertThrows(ValidacaoException.class, () ->
      validator.validarNovoUsuario("12345678901", "usuario@email.com")
    );

    assertEquals(
      "Já existe um usuário cadastrado com este e-mail.",
      erro.getMessage()
    );
  }

  @Test
  void deveAceitarCpfEEmailDisponiveis() {
    when(usuarioRepository.existsByCpf("12345678901")).thenReturn(false);
    when(usuarioRepository.existsByEmail("usuario@email.com")).thenReturn(
      false
    );

    assertDoesNotThrow(() ->
      validator.validarNovoUsuario("12345678901", "usuario@email.com")
    );
  }
}
