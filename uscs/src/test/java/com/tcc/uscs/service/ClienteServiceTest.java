package com.tcc.uscs.service;

import static org.mockito.Mockito.*;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.cliente.Cliente;
import com.tcc.uscs.model.cliente.dto.AtualizarClienteDTO;
import com.tcc.uscs.model.cliente.dto.CadastrarClienteDTO;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.ClienteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.StoredProcedureQuery;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

  @InjectMocks
  private ClienteService clienteService;

  @Mock
  private ClienteRepository repository;

  @Mock
  private EntityManager entityManager;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @Mock
  private Usuario usuarioLogado;

  @Mock
  private StoredProcedureQuery storedProcedureQuery;

  private void mockUsuarioLogado(Long id, String role) {
    lenient()
      .when(securityContext.getAuthentication())
      .thenReturn(authentication);
    lenient().when(authentication.getPrincipal()).thenReturn(usuarioLogado);
    lenient().when(usuarioLogado.getId()).thenReturn(id);
    lenient()
      .doReturn(List.of(new SimpleGrantedAuthority(role)))
      .when(usuarioLogado)
      .getAuthorities();

    SecurityContextHolder.setContext(securityContext);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName(
    "Deveria lançar erro ao tentar obter cliente inexistente ou inativo"
  )
  void cenarioObterEntidadeInexistente() {
    when(repository.findById(1L)).thenReturn(Optional.empty());

    var excecao = Assertions.assertThrows(ValidacaoException.class, () ->
      clienteService.obterEntidadePorId(1L)
    );

    Assertions.assertEquals(
      "Cliente não encontrado ou inativo!",
      excecao.getMessage()
    );
  }

  @Test
  @DisplayName(
    "Deveria cadastrar cliente com sucesso executando a Stored Procedure"
  )
  void cenarioCadastrarComSucesso() {
    var dtoCadastro = mock(CadastrarClienteDTO.class);
    when(dtoCadastro.senha()).thenReturn("123456");

    when(passwordEncoder.encode("123456")).thenReturn("hashedPassword");
    when(
      entityManager.createStoredProcedureQuery("sp_cadastrar_usuario_cliente")
    ).thenReturn(storedProcedureQuery);
    when(storedProcedureQuery.getOutputParameterValue("p_id")).thenReturn(1L);

    var clienteMock = mock(Cliente.class);
    var usuarioMock = mock(Usuario.class);
    when(clienteMock.getUsuario()).thenReturn(usuarioMock);
    when(repository.findById(1L)).thenReturn(Optional.of(clienteMock));

    var resultado = clienteService.cadastrar(dtoCadastro);

    verify(storedProcedureQuery).execute();
    Assertions.assertNotNull(resultado);
  }

  @Test
  @DisplayName(
    "Deveria permitir que um Funcionário detalhe os dados de qualquer cliente"
  )
  void cenarioDetalharPorFuncionario() {
    mockUsuarioLogado(99L, "ROLE_FUNCIONARIO");

    var clienteMock = mock(Cliente.class);
    var usuarioMock = mock(Usuario.class);
    when(clienteMock.getUsuario()).thenReturn(usuarioMock);
    when(repository.findById(1L)).thenReturn(Optional.of(clienteMock));

    var resultado = clienteService.detalhar(1L);

    Assertions.assertNotNull(resultado);
  }

  @Test
  @DisplayName("Deveria permitir que um Cliente detalhe seus próprios dados")
  void cenarioDetalharPeloProprioCliente() {
    mockUsuarioLogado(1L, "ROLE_CLIENTE");

    var clienteMock = mock(Cliente.class);
    var usuarioMock = mock(Usuario.class);
    when(clienteMock.getUsuario()).thenReturn(usuarioMock);
    when(repository.findById(1L)).thenReturn(Optional.of(clienteMock));

    var resultado = clienteService.detalhar(1L);

    Assertions.assertNotNull(resultado);
  }

  @Test
  @DisplayName(
    "Deveria lançar AccessDeniedException se um Cliente tentar acessar dados de outro cliente"
  )
  void cenarioDetalharOutroClienteNegado() {
    mockUsuarioLogado(2L, "ROLE_CLIENTE");

    var excecao = Assertions.assertThrows(AccessDeniedException.class, () ->
      clienteService.detalhar(1L)
    );

    Assertions.assertEquals(
      "Você não tem permissão para acessar ou alterar dados deste cliente.",
      excecao.getMessage()
    );
  }

  @Test
  @DisplayName("Deveria atualizar dados do cliente com sucesso")
  void cenarioAtualizarComSucesso() {
    mockUsuarioLogado(1L, "ROLE_CLIENTE");

    var dtoAtualizar = mock(AtualizarClienteDTO.class);
    var clienteMock = mock(Cliente.class);
    var usuarioMock = mock(Usuario.class);

    when(clienteMock.getUsuario()).thenReturn(usuarioMock);
    when(repository.findById(1L)).thenReturn(Optional.of(clienteMock));

    var resultado = clienteService.atualizar(1L, dtoAtualizar);

    verify(clienteMock).atualizar(dtoAtualizar);
    Assertions.assertNotNull(resultado);
  }

  @Test
  @DisplayName("Deveria excluir cliente chamando o método de exclusão lógica")
  void cenarioExcluirComSucesso() {
    var clienteMock = mock(Cliente.class);
    when(repository.findById(1L)).thenReturn(Optional.of(clienteMock));

    clienteService.excluir(1L);

    verify(clienteMock).excluir();
  }
}
