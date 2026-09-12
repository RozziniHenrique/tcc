package com.tcc.uscs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tcc.uscs.model.funcionario.Funcao;
import com.tcc.uscs.model.funcionario.dto.CadastrarFuncionarioDTO;
import com.tcc.uscs.repository.FuncionarioRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdministradorInicialServiceTest {

  @Mock
  private FuncionarioRepository funcionarioRepository;

  @Mock
  private FuncionarioService funcionarioService;

  @Mock
  private Validator validator;

  @InjectMocks
  private AdministradorInicialService service;

  @BeforeEach
  void configurar() {
    ReflectionTestUtils.setField(service, "nome", "Administrador");
    ReflectionTestUtils.setField(service, "cpf", "12345678901");
    ReflectionTestUtils.setField(service, "email", "admin@stfer.com");
    ReflectionTestUtils.setField(service, "senha", "senhaSegura123");
    ReflectionTestUtils.setField(service, "telefone", "11999999999");
    ReflectionTestUtils.setField(service, "endereco", "São Paulo");
  }

  @Test
  void naoDeveDuplicarAdministradorExistente() {
    when(
      funcionarioRepository.existsByFuncaoAndAtivoTrueAndUsuarioAtivoTrue(
        Funcao.ADMIN
      )
    ).thenReturn(true);

    service.run(null);

    verifyNoInteractions(funcionarioService, validator);
  }

  @Test
  void deveCriarPrimeiroAdministrador() {
    when(
      funcionarioRepository.existsByFuncaoAndAtivoTrueAndUsuarioAtivoTrue(
        Funcao.ADMIN
      )
    ).thenReturn(false);

    doReturn(Set.of())
      .when(validator)
      .validate(any(CadastrarFuncionarioDTO.class));

    service.run(null);

    var captor = ArgumentCaptor.forClass(CadastrarFuncionarioDTO.class);

    verify(funcionarioService).cadastrar(captor.capture());

    var dados = captor.getValue();

    assertEquals("Administrador", dados.nome());
    assertEquals("admin@stfer.com", dados.email());
    assertEquals(Funcao.ADMIN, dados.funcao());
  }

  @Test
  @SuppressWarnings("unchecked")
  void deveRecusarConfiguracaoInvalida() {
    when(
      funcionarioRepository.existsByFuncaoAndAtivoTrueAndUsuarioAtivoTrue(
        Funcao.ADMIN
      )
    ).thenReturn(false);

    ConstraintViolation<CadastrarFuncionarioDTO> violacao = mock(
      ConstraintViolation.class
    );
    Path caminho = mock(Path.class);

    when(violacao.getPropertyPath()).thenReturn(caminho);
    when(caminho.toString()).thenReturn("email");

    doReturn(Set.of(violacao))
      .when(validator)
      .validate(any(CadastrarFuncionarioDTO.class));

    var erro = assertThrows(IllegalStateException.class, () ->
      service.run(null)
    );

    assertTrue(erro.getMessage().contains("email"));
    verifyNoInteractions(funcionarioService);
  }
}
