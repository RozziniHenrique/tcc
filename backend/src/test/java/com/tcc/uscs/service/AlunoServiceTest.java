package com.tcc.uscs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tcc.uscs.infra.exception.RecursoNaoEncontradoException;
import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.aluno.Aluno;
import com.tcc.uscs.model.aluno.dto.AtualizarAlunoDTO;
import com.tcc.uscs.model.aluno.dto.CadastrarAlunoDTO;
import com.tcc.uscs.model.curso.Curso;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.AlunoRepository;
import com.tcc.uscs.repository.CursoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.StoredProcedureQuery;
import java.time.LocalDateTime;
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

  @Mock
  private CadastroUsuarioValidator cadastroUsuarioValidator;

  @Mock
  private CursoRepository cursoRepository;

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
  @DisplayName("Deveria retornar ID de aluno disponível para o curso e horário")
  void cenarioBuscarAlunoAleatorioSucesso() {
    var dataHora = LocalDateTime.of(2026, 10, 1, 14, 0);
    var alunoMock = mock(Aluno.class);

    when(alunoMock.getId()).thenReturn(10L);
    when(repository.buscarDisponiveisPorCursoEHorario(1L, dataHora)).thenReturn(
      List.of(alunoMock)
    );

    var idAluno = alunoService.buscarAlunoAleatorio(1L, dataHora);

    Assertions.assertEquals(10L, idAluno);
  }

  @Test
  @DisplayName(
    "Deveria lançar erro quando não houver aluno disponível no horário"
  )
  void cenarioBuscarAlunoAleatorioSemDisponibilidade() {
    var dataHora = LocalDateTime.of(2026, 10, 1, 14, 0);

    when(repository.buscarDisponiveisPorCursoEHorario(1L, dataHora)).thenReturn(
      Collections.emptyList()
    );

    var excecao = Assertions.assertThrows(ValidacaoException.class, () ->
      alunoService.buscarAlunoAleatorio(1L, dataHora)
    );

    Assertions.assertEquals(
      "Nenhum aluno disponível para este curso e horário.",
      excecao.getMessage()
    );
  }

  @Test
  @DisplayName("Deveria contar alunos disponíveis para o curso e horário")
  void cenarioContarAlunosDisponiveis() {
    var dataHora = LocalDateTime.of(2026, 10, 1, 14, 0);

    when(
      repository.contarDisponiveisPorCursoEHorario(1L, dataHora)
    ).thenReturn(3L);

    var quantidade = alunoService.contarAlunosDisponiveis(1L, dataHora);

    assertEquals(3L, quantidade);
  }

  @Test
  @DisplayName("Deveria cadastrar aluno com sucesso via Stored Procedure")
  void cenarioCadastrarComSucesso() {
    var curso = mock(Curso.class);

    when(cursoRepository.findByIdAndAtivoTrue(1L)).thenReturn(
      Optional.of(curso)
    );
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
    when(repository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(5L)).thenReturn(
      Optional.of(alunoMock)
    );

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
    when(dtoAtualizar.idCurso()).thenReturn(null);
    var alunoMock = mock(Aluno.class);
    var usuarioMock = mock(Usuario.class);

    when(alunoMock.getUsuario()).thenReturn(usuarioMock);
    when(repository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(1L)).thenReturn(
      Optional.of(alunoMock)
    );

    var resultado = alunoService.atualizar(1L, dtoAtualizar);

    verify(alunoMock).atualizar(dtoAtualizar);
    Assertions.assertNotNull(resultado);
  }

  @Test
  @DisplayName("Deveria permitir alterar o curso do aluno")
  void cenarioAtualizarCursoDoAluno() {
    mockUsuarioLogado(1L, "ROLE_ALUNO");

    var dados = mock(AtualizarAlunoDTO.class);
    var aluno = mock(Aluno.class);
    var usuario = mock(Usuario.class);
    var curso = mock(Curso.class);

    when(dados.idCurso()).thenReturn(10L);
    when(aluno.getUsuario()).thenReturn(usuario);
    when(repository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(1L)).thenReturn(
      Optional.of(aluno)
    );
    when(cursoRepository.findByIdAndAtivoTrue(10L)).thenReturn(
      Optional.of(curso)
    );

    var resultado = alunoService.atualizar(1L, dados);

    verify(aluno).setCurso(curso);
    assertNotNull(resultado);
  }

  @Test
  @DisplayName("Deveria recusar curso inexistente ou inativo")
  void cenarioAtualizarParaCursoInvalido() {
    mockUsuarioLogado(1L, "ROLE_ALUNO");

    var dados = mock(AtualizarAlunoDTO.class);
    var aluno = mock(Aluno.class);

    when(dados.idCurso()).thenReturn(10L);
    when(repository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(1L)).thenReturn(
      Optional.of(aluno)
    );
    when(cursoRepository.findByIdAndAtivoTrue(10L)).thenReturn(
      Optional.empty()
    );

    var erro = assertThrows(RecursoNaoEncontradoException.class, () ->
      alunoService.atualizar(1L, dados)
    );

    assertEquals("Curso não encontrado ou inativo.", erro.getMessage());

    verify(aluno, never()).setCurso(any(Curso.class));
  }

  @Test
  @DisplayName("Deveria obter aluno ativo pertencente ao curso")
  void cenarioObterAlunoDoCurso() {
    var aluno = mock(Aluno.class);
    var curso = mock(Curso.class);

    when(repository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(1L)).thenReturn(
      Optional.of(aluno)
    );
    when(aluno.getCurso()).thenReturn(curso);
    when(curso.getId()).thenReturn(2L);

    var resultado = alunoService.obterEntidadePorIdECurso(1L, 2L);

    assertSame(aluno, resultado);
  }

  @Test
  @DisplayName("Deveria recusar aluno que não pertence ao curso")
  void cenarioObterAlunoDeOutroCurso() {
    var aluno = mock(Aluno.class);
    var curso = mock(Curso.class);

    when(repository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(1L)).thenReturn(
      Optional.of(aluno)
    );
    when(aluno.getCurso()).thenReturn(curso);
    when(curso.getId()).thenReturn(3L);

    var erro = assertThrows(ValidacaoException.class, () ->
      alunoService.obterEntidadePorIdECurso(1L, 2L)
    );

    assertEquals(
      "O aluno informado não pertence ao curso selecionado.",
      erro.getMessage()
    );
  }

  @Test
  @DisplayName("Deveria recusar cadastro com curso inexistente ou inativo")
  void cenarioCadastrarComCursoInvalido() {
    var dados = mock(CadastrarAlunoDTO.class);

    when(dados.idCurso()).thenReturn(999L);
    when(cursoRepository.findByIdAndAtivoTrue(999L)).thenReturn(
      Optional.empty()
    );

    var erro = assertThrows(RecursoNaoEncontradoException.class, () ->
      alunoService.cadastrar(dados)
    );

    assertEquals("Curso não encontrado ou inativo.", erro.getMessage());
    verifyNoInteractions(entityManager, passwordEncoder);
  }

  @Test
  void deveriaRecusarAtualizacaoVazia() {
    var dados = new AtualizarAlunoDTO(null, null, null, null, null);

    var erro = assertThrows(ValidacaoException.class, () ->
      alunoService.atualizar(1L, dados)
    );

    assertEquals(
      "Informe pelo menos um campo para realizar a atualização.",
      erro.getMessage()
    );
    verifyNoInteractions(repository);
  }
}
