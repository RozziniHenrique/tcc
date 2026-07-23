package com.tcc.uscs.service;

import static org.mockito.Mockito.*;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.funcionario.Funcionario;
import com.tcc.uscs.model.funcionario.dto.AtualizarFuncionarioDTO;
import com.tcc.uscs.model.funcionario.dto.CadastrarFuncionarioDTO;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.FuncionarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.StoredProcedureQuery;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

  @InjectMocks
  private FuncionarioService funcionarioService;

  @Mock
  private FuncionarioRepository repository;

  @Mock
  private EntityManager entityManager;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private StoredProcedureQuery storedProcedureQuery;

  @Test
  @DisplayName(
    "Deveria lançar erro ao obter funcionário não encontrado ou inativo"
  )
  void cenarioObterEntidadeInexistente() {
    when(repository.findById(1L)).thenReturn(Optional.empty());

    var excecao = Assertions.assertThrows(ValidacaoException.class, () ->
      funcionarioService.obterEntidadePorId(1L)
    );

    Assertions.assertEquals(
      "Funcionário não encontrado ou inativo!",
      excecao.getMessage()
    );
  }

  @Test
  @DisplayName("Deveria cadastrar funcionário com sucesso via Stored Procedure")
  void cenarioCadastrarComSucesso() {
    var dtoCadastro = mock(CadastrarFuncionarioDTO.class);
    when(dtoCadastro.senha()).thenReturn("123456");

    when(passwordEncoder.encode("123456")).thenReturn("hashedPassword");
    when(
      entityManager.createStoredProcedureQuery(
        "sp_cadastrar_usuario_funcionario"
      )
    ).thenReturn(storedProcedureQuery);
    when(storedProcedureQuery.getOutputParameterValue("p_id")).thenReturn(1L);

    var funcionarioMock = mock(Funcionario.class);
    var usuarioMock = mock(Usuario.class);
    when(funcionarioMock.getUsuario()).thenReturn(usuarioMock);
    when(repository.findById(1L)).thenReturn(Optional.of(funcionarioMock));

    var resultado = funcionarioService.cadastrar(dtoCadastro);

    verify(storedProcedureQuery).execute();
    Assertions.assertNotNull(resultado);
  }

  @Test
  @DisplayName("Deveria detalhar funcionário existente com sucesso")
  void cenarioDetalharComSucesso() {
    var funcionarioMock = mock(Funcionario.class);
    var usuarioMock = mock(Usuario.class);
    when(funcionarioMock.getUsuario()).thenReturn(usuarioMock);
    when(repository.findById(1L)).thenReturn(Optional.of(funcionarioMock));

    var resultado = funcionarioService.detalhar(1L);

    Assertions.assertNotNull(resultado);
  }

  @Test
  @DisplayName("Deveria atualizar funcionário com sucesso")
  void cenarioAtualizarComSucesso() {
    var dtoAtualizar = mock(AtualizarFuncionarioDTO.class);
    var funcionarioMock = mock(Funcionario.class);
    var usuarioMock = mock(Usuario.class);

    when(funcionarioMock.getUsuario()).thenReturn(usuarioMock);
    when(repository.findById(1L)).thenReturn(Optional.of(funcionarioMock));

    var resultado = funcionarioService.atualizar(1L, dtoAtualizar);

    verify(funcionarioMock).atualizar(dtoAtualizar);
    Assertions.assertNotNull(resultado);
  }

  @Test
  @DisplayName("Deveria realizar a exclusão lógica do funcionário")
  void cenarioExcluirComSucesso() {
    var funcionarioMock = mock(Funcionario.class);
    when(repository.findById(1L)).thenReturn(Optional.of(funcionarioMock));

    funcionarioService.excluir(1L);

    verify(funcionarioMock).excluir();
  }
}
