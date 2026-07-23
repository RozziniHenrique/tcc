package com.tcc.uscs.service;

import static org.mockito.Mockito.*;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.aluno.Aluno;
import com.tcc.uscs.model.aluno.dto.AtualizarAlunoDTO;
import com.tcc.uscs.model.aluno.dto.CadastrarAlunoDTO;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.AlunoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.StoredProcedureQuery;
import java.util.Collections;
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
class AlunoServiceTest {

  @InjectMocks
  private AlunoService alunoService;

  @Mock
  private AlunoRepository repository;

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
  @DisplayName("Deveria retornar ID de aluno aleatório disponível para o curso")
  void cenarioBuscarAlunoAleatorioSucesso() {
    var alunoMock = mock(Aluno.class);
    when(alunoMock.getId()).thenReturn(10L);

    when(repository.findAllByCursoIdAndUsuarioAtivoTrue(1L)).thenReturn(
      List.of(alunoMock)
    );

    var idAluno = alunoService.buscarAlunoAleatorio(1L);

    Assertions.assertEquals(10L, idAluno);
  }

  @Test
  @DisplayName(
    "Deveria lançar erro ao buscar aluno aleatório se não houver nenhum disponível"
  )
  void cenarioBuscarAlunoAleatorioSemDisponibilidade() {
    when(repository.findAllByCursoIdAndUsuarioAtivoTrue(1L)).thenReturn(
      Collections.emptyList()
    );

    var excecao = Assertions.assertThrows(ValidacaoException.class, () ->
      alunoService.buscarAlunoAleatorio(1L)
    );

    Assertions.assertEquals(
      "Nenhum aluno disponível para este curso.",
      excecao.getMessage()
    );
  }

  @Test
  @DisplayName("Deveria cadastrar aluno com sucesso via Stored Procedure")
  void cenarioCadastrarComSucesso() {
    var dtoCadastro = mock(CadastrarAlunoDTO.class);
    when(dtoCadastro.senha()).thenReturn("123456");
    when(dtoCadastro.idCurso()).thenReturn(1L);

    when(passwordEncoder.encode("123456")).thenReturn("hashedPassword");
    when(
      entityManager.createStoredProcedureQuery("sp_cadastrar_usuario_aluno")
    ).thenReturn(storedProcedureQuery);
    when(storedProcedureQuery.getOutputParameterValue("p_id")).thenReturn(5L);

    var alunoMock = mock(Aluno.class);
    var usuarioMock = mock(Usuario.class);
    when(alunoMock.getUsuario()).thenReturn(usuarioMock);
    when(repository.findById(5L)).thenReturn(Optional.of(alunoMock));

    var resultado = alunoService.cadastrar(dtoCadastro);

    verify(storedProcedureQuery).execute();
    Assertions.assertNotNull(resultado);
  }

  @Test
  @DisplayName(
    "Deveria lançar AccessDeniedException se um Aluno tentar acessar perfil de outro aluno"
  )
  void cenarioDetalharOutroAlunoNegado() {
    mockUsuarioLogado(2L, "ROLE_ALUNO");

    var excecao = Assertions.assertThrows(AccessDeniedException.class, () ->
      alunoService.detalhar(1L)
    );

    Assertions.assertEquals(
      "Você não tem permissão para acessar ou alterar dados deste aluno.",
      excecao.getMessage()
    );
  }

  @Test
  @DisplayName("Deveria permitir que o próprio Aluno atualize seus dados")
  void cenarioAtualizarPeloProprioAluno() {
    mockUsuarioLogado(1L, "ROLE_ALUNO");

    var dtoAtualizar = mock(AtualizarAlunoDTO.class);
    var alunoMock = mock(Aluno.class);
    var usuarioMock = mock(Usuario.class);

    when(alunoMock.getUsuario()).thenReturn(usuarioMock);
    when(repository.findById(1L)).thenReturn(Optional.of(alunoMock));

    var resultado = alunoService.atualizar(1L, dtoAtualizar);

    verify(alunoMock).atualizar(dtoAtualizar);
    Assertions.assertNotNull(resultado);
  }
}
