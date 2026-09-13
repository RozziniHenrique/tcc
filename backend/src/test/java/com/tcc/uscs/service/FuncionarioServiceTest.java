package com.tcc.uscs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.tcc.uscs.infra.exception.RecursoNaoEncontradoException;
import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.funcionario.Funcao;
import com.tcc.uscs.model.funcionario.Funcionario;
import com.tcc.uscs.model.funcionario.dto.AdicionarPerfilFuncionarioDTO;
import com.tcc.uscs.model.funcionario.dto.AtualizarFuncionarioDTO;
import com.tcc.uscs.model.funcionario.dto.CadastrarFuncionarioDTO;
import com.tcc.uscs.model.usuario.TipoUsuario;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.FuncionarioRepository;
import com.tcc.uscs.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.StoredProcedureQuery;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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

  @Mock
  private CadastroUsuarioValidator cadastroUsuarioValidator;

  @Mock
  private UsuarioRepository usuarioRepository;

  @Test
  @DisplayName(
    "Deveria lançar erro ao obter funcionário não encontrado ou inativo"
  )
  void cenarioObterEntidadeInexistente() {
    when(repository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(1L)).thenReturn(
      Optional.empty()
    );

    var excecao = Assertions.assertThrows(
      RecursoNaoEncontradoException.class,
      () -> funcionarioService.obterEntidadePorId(1L)
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
    when(repository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(1L)).thenReturn(
      Optional.of(funcionarioMock)
    );

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
    when(repository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(1L)).thenReturn(
      Optional.of(funcionarioMock)
    );

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
    when(repository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(1L)).thenReturn(
      Optional.of(funcionarioMock)
    );

    var resultado = funcionarioService.atualizar(1L, dtoAtualizar);

    verify(funcionarioMock).atualizar(dtoAtualizar);
    Assertions.assertNotNull(resultado);
  }

  @Test
  @DisplayName("Deveria realizar a exclusão lógica do funcionário")
  void cenarioExcluirComSucesso() {
    var funcionarioMock = mock(Funcionario.class);
    when(repository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(1L)).thenReturn(
      Optional.of(funcionarioMock)
    );

    funcionarioService.excluir(1L);

    verify(funcionarioMock).excluir();
  }

  @Test
  @DisplayName("Deveria adicionar perfil de funcionário a uma conta existente")
  void cenarioAdicionarPerfilFuncionario() {
    var usuario = mock(Usuario.class);
    var perfis = new HashSet<>(Set.of(TipoUsuario.CLIENTE));

    when(usuario.isEnabled()).thenReturn(true);
    when(usuario.getPerfis()).thenReturn(perfis);
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

    var resultado = funcionarioService.adicionarPerfil(
      1L,
      new AdicionarPerfilFuncionarioDTO(Funcao.PROFESSOR)
    );

    assertTrue(perfis.contains(TipoUsuario.FUNCIONARIO));
    assertEquals(Funcao.PROFESSOR, resultado.funcao());

    verify(repository).save(
      argThat(
        funcionario ->
          funcionario.getUsuario().equals(usuario) &&
          funcionario.getFuncao() == Funcao.PROFESSOR
      )
    );
  }

  @Test
  @DisplayName("Deveria recusar perfil de funcionário duplicado")
  void cenarioAdicionarPerfilFuncionarioDuplicado() {
    var usuario = mock(Usuario.class);
    var perfis = new HashSet<>(Set.of(TipoUsuario.FUNCIONARIO));

    when(usuario.isEnabled()).thenReturn(true);
    when(usuario.getPerfis()).thenReturn(perfis);
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

    var erro = assertThrows(ValidacaoException.class, () ->
      funcionarioService.adicionarPerfil(
        1L,
        new AdicionarPerfilFuncionarioDTO(Funcao.PROFESSOR)
      )
    );

    assertEquals(
      "O usuário já possui o perfil FUNCIONARIO.",
      erro.getMessage()
    );

    verify(repository, never()).save(any(Funcionario.class));
  }

  @Test
  @DisplayName("Deveria recusar usuário inexistente ao adicionar perfil")
  void cenarioAdicionarPerfilEmUsuarioInexistente() {
    when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

    var erro = assertThrows(RecursoNaoEncontradoException.class, () ->
      funcionarioService.adicionarPerfil(
        1L,
        new AdicionarPerfilFuncionarioDTO(Funcao.PROFESSOR)
      )
    );

    assertEquals("Usuário não encontrado ou inativo.", erro.getMessage());

    verify(repository, never()).save(any(Funcionario.class));
  }

  @Test
  void deveriaRecusarAtualizacaoVazia() {
    var dados = new AtualizarFuncionarioDTO(null, null, null, null, null);

    var erro = assertThrows(ValidacaoException.class, () ->
      funcionarioService.atualizar(1L, dados)
    );

    assertEquals(
      "Informe pelo menos um campo para realizar a atualização.",
      erro.getMessage()
    );
    verifyNoInteractions(repository);
  }

  @Test
  void deveriaImpedirExclusaoDoUltimoAdministrador() {
    var funcionario = mock(Funcionario.class);

    when(funcionario.getFuncao()).thenReturn(Funcao.ADMIN);
    when(repository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(1L)).thenReturn(
      Optional.of(funcionario)
    );
    when(
      repository.countByFuncaoAndAtivoTrueAndUsuarioAtivoTrue(Funcao.ADMIN)
    ).thenReturn(1L);

    var erro = assertThrows(ValidacaoException.class, () ->
      funcionarioService.excluir(1L)
    );

    assertEquals(
      "Não é possível remover o último administrador ativo.",
      erro.getMessage()
    );
    verify(funcionario, never()).excluir();
  }

  @Test
  void deveriaImpedirRebaixamentoDoUltimoAdministrador() {
    var funcionario = mock(Funcionario.class);
    var dados = new AtualizarFuncionarioDTO(
      null,
      null,
      null,
      null,
      Funcao.GESTOR
    );

    when(funcionario.getFuncao()).thenReturn(Funcao.ADMIN);
    when(repository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(1L)).thenReturn(
      Optional.of(funcionario)
    );
    when(
      repository.countByFuncaoAndAtivoTrueAndUsuarioAtivoTrue(Funcao.ADMIN)
    ).thenReturn(1L);

    var erro = assertThrows(ValidacaoException.class, () ->
      funcionarioService.atualizar(1L, dados)
    );

    assertEquals(
      "Não é possível remover o último administrador ativo.",
      erro.getMessage()
    );
    verify(funcionario, never()).atualizar(any());
  }

  @Test
  void deveriaPermitirExcluirAdministradorQuandoExisteOutro() {
    var funcionario = mock(Funcionario.class);

    when(funcionario.getFuncao()).thenReturn(Funcao.ADMIN);
    when(repository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(1L)).thenReturn(
      Optional.of(funcionario)
    );
    when(
      repository.countByFuncaoAndAtivoTrueAndUsuarioAtivoTrue(Funcao.ADMIN)
    ).thenReturn(2L);

    funcionarioService.excluir(1L);

    verify(funcionario).excluir();
  }
}
