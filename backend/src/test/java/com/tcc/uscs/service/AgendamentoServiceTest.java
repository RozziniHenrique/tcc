package com.tcc.uscs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import com.tcc.uscs.infra.exception.RecursoNaoEncontradoException;
import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.agendamento.Agendamento;
import com.tcc.uscs.model.agendamento.StatusAgendamento;
import com.tcc.uscs.model.agendamento.dto.AtualizarAgendamentoDTO;
import com.tcc.uscs.model.agendamento.dto.CadastrarAgendamentoDTO;
import com.tcc.uscs.model.aluno.Aluno;
import com.tcc.uscs.model.cliente.Cliente;
import com.tcc.uscs.model.curso.Curso;
import com.tcc.uscs.model.servico.Servico;
import com.tcc.uscs.model.unidade.Unidade;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.AgendamentoRepository;
import com.tcc.uscs.repository.ClienteRepository;
import com.tcc.uscs.repository.CursoRepository;
import com.tcc.uscs.repository.UnidadeRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

  @InjectMocks
  private AgendamentoService agendamentoService;

  @Mock
  private AgendamentoRepository repository;

  @Mock
  private ClienteRepository clienteRepository;

  @Mock
  private CursoRepository cursoRepository;

  @Mock
  private UnidadeRepository unidadeRepository;

  @Mock
  private AlunoService alunoService;

  @Mock
  private ServicoService servicoService;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @Mock
  private Usuario usuarioLogado;

  @BeforeEach
  void setupSecurity() {
    lenient()
      .when(securityContext.getAuthentication())
      .thenReturn(authentication);
    lenient().when(authentication.getPrincipal()).thenReturn(usuarioLogado);
    lenient().when(usuarioLogado.getId()).thenReturn(1L);

    lenient()
      .doReturn(List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO")))
      .when(usuarioLogado)
      .getAuthorities();
    SecurityContextHolder.setContext(securityContext);
  }

  @AfterEach
  void clearSecurity() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName(
    "Deveria lançar erro ao tentar agendar com menos de 30 minutos de antecedência"
  )
  void cenarioAntecedenciaMinima() {
    var dataInvalida = LocalDateTime.now().plusMinutes(10);

    var dto = new CadastrarAgendamentoDTO(
      1L,
      1L,
      1L,
      1L,
      List.of(1L),
      dataInvalida
    );

    when(
      clienteRepository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(1L)
    ).thenReturn(Optional.of(mock(Cliente.class)));
    when(alunoService.obterEntidadePorIdECurso(1L, 1L)).thenReturn(
      mock(Aluno.class)
    );
    when(cursoRepository.findByIdAndAtivoTrue(1L)).thenReturn(
      Optional.of(mock(Curso.class))
    );
    when(unidadeRepository.findByIdAndAtivoTrue(1L)).thenReturn(
      Optional.of(mock(Unidade.class))
    );

    var excecao = Assertions.assertThrows(ValidacaoException.class, () ->
      agendamentoService.agendar(dto)
    );

    Assertions.assertEquals(
      "Antecedência mínima de 30 minutos exigida.",
      excecao.getMessage()
    );
  }

  @Test
  @DisplayName("Deveria lançar erro ao tentar agendar em um domingo")
  void cenarioForaHorarioComercialDomingo() {
    var domingo = LocalDateTime.now()
      .plusWeeks(1)
      .with(DayOfWeek.SUNDAY)
      .withHour(10)
      .withMinute(0);

    var dto = new CadastrarAgendamentoDTO(1L, 1L, 1L, 1L, List.of(1L), domingo);

    when(
      clienteRepository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(1L)
    ).thenReturn(Optional.of(mock(Cliente.class)));
    when(alunoService.obterEntidadePorIdECurso(1L, 1L)).thenReturn(
      mock(Aluno.class)
    );
    when(cursoRepository.findByIdAndAtivoTrue(1L)).thenReturn(
      Optional.of(mock(Curso.class))
    );
    when(unidadeRepository.findByIdAndAtivoTrue(1L)).thenReturn(
      Optional.of(mock(Unidade.class))
    );

    var excecao = Assertions.assertThrows(ValidacaoException.class, () ->
      agendamentoService.agendar(dto)
    );

    Assertions.assertEquals(
      "Fora do horário comercial (Seg-Sáb, 08h-19h).",
      excecao.getMessage()
    );
  }

  @Test
  @DisplayName(
    "Deveria lançar erro ao tentar agendar no mesmo horário para um aluno que já tem compromisso"
  )
  void cenarioConflitoHorarioAluno() {
    var dataComercialValida = LocalDateTime.now()
      .plusWeeks(1)
      .with(DayOfWeek.TUESDAY)
      .withHour(14)
      .withMinute(0);

    var dto = new CadastrarAgendamentoDTO(
      1L,
      1L,
      1L,
      1L,
      List.of(1L),
      dataComercialValida
    );

    when(
      clienteRepository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(1L)
    ).thenReturn(Optional.of(mock(Cliente.class)));
    when(alunoService.obterEntidadePorIdECurso(1L, 1L)).thenReturn(
      mock(Aluno.class)
    );
    when(cursoRepository.findByIdAndAtivoTrue(1L)).thenReturn(
      Optional.of(mock(Curso.class))
    );
    when(unidadeRepository.findByIdAndAtivoTrue(1L)).thenReturn(
      Optional.of(mock(Unidade.class))
    );

    when(
      repository.existsByAlunoIdAndDataHoraAndAtivoTrue(1L, dataComercialValida)
    ).thenReturn(true);

    var excecao = Assertions.assertThrows(ValidacaoException.class, () ->
      agendamentoService.agendar(dto)
    );

    Assertions.assertEquals(
      "O aluno já possui agendamento neste horário.",
      excecao.getMessage()
    );
  }

  @Test
  @DisplayName(
    "Deveria lançar erro ao tentar agendar no mesmo horário para um cliente que já tem compromisso"
  )
  void cenarioConflitoHorarioCliente() {
    var dataComercialValida = LocalDateTime.now()
      .plusWeeks(1)
      .with(DayOfWeek.TUESDAY)
      .withHour(14)
      .withMinute(0);

    var dto = new CadastrarAgendamentoDTO(
      1L,
      1L,
      1L,
      1L,
      List.of(1L),
      dataComercialValida
    );

    when(
      clienteRepository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(1L)
    ).thenReturn(Optional.of(mock(Cliente.class)));
    when(alunoService.obterEntidadePorIdECurso(1L, 1L)).thenReturn(
      mock(Aluno.class)
    );
    when(cursoRepository.findByIdAndAtivoTrue(1L)).thenReturn(
      Optional.of(mock(Curso.class))
    );
    when(unidadeRepository.findByIdAndAtivoTrue(1L)).thenReturn(
      Optional.of(mock(Unidade.class))
    );

    when(
      repository.existsByAlunoIdAndDataHoraAndAtivoTrue(1L, dataComercialValida)
    ).thenReturn(false);
    when(
      repository.existsByClienteIdAndDataHoraAndAtivoTrue(
        1L,
        dataComercialValida
      )
    ).thenReturn(true);

    var excecao = Assertions.assertThrows(ValidacaoException.class, () ->
      agendamentoService.agendar(dto)
    );

    Assertions.assertEquals(
      "O cliente já possui agendamento neste horário.",
      excecao.getMessage()
    );
  }

  @Test
  @DisplayName(
    "Deveria realizar o agendamento com sucesso calculando o valor total dos serviços"
  )
  void cenarioAgendamentoComSucesso() {
    var dataValida = LocalDateTime.now()
      .plusWeeks(1)
      .with(DayOfWeek.TUESDAY)
      .withHour(14)
      .withMinute(0);

    var dto = new CadastrarAgendamentoDTO(
      1L,
      1L,
      1L,
      1L,
      List.of(1L, 2L),
      dataValida
    );

    var usuarioFake = mock(Usuario.class);
    when(usuarioFake.getNome()).thenReturn("Nome de Teste");

    var clienteMock = mock(Cliente.class);
    when(clienteMock.getUsuario()).thenReturn(usuarioFake);

    var alunoMock = mock(Aluno.class);
    when(alunoMock.getUsuario()).thenReturn(usuarioFake);

    var cursoMock = mock(Curso.class);
    var unidadeMock = mock(Unidade.class);

    when(
      clienteRepository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(1L)
    ).thenReturn(Optional.of(clienteMock));
    when(alunoService.obterEntidadePorIdECurso(1L, 1L)).thenReturn(alunoMock);
    when(cursoRepository.findByIdAndAtivoTrue(1L)).thenReturn(
      Optional.of(cursoMock)
    );
    when(unidadeRepository.findByIdAndAtivoTrue(1L)).thenReturn(
      Optional.of(unidadeMock)
    );

    var s1 = mock(Servico.class);
    when(s1.getValor()).thenReturn(new BigDecimal("100.00"));

    var s2 = mock(Servico.class);
    when(s2.getValor()).thenReturn(new BigDecimal("50.00"));

    when(servicoService.buscarServicosValidos(dto.idServicos())).thenReturn(
      List.of(s1, s2)
    );

    var resultado = agendamentoService.agendar(dto);

    verify(repository).save(any(Agendamento.class));
    Assertions.assertNotNull(resultado);
  }

  @Test
  @DisplayName(
    "Deveria cancelar um agendamento com sucesso se tiver mais de 24h de antecedência"
  )
  void cenarioCancelarComSucesso() {
    var agendamentoMock = mock(Agendamento.class);
    when(agendamentoMock.getDataHora()).thenReturn(
      LocalDateTime.now().plusDays(3)
    );
    when(agendamentoMock.getStatus()).thenReturn(StatusAgendamento.AGENDADO);
    when(repository.findById(1L)).thenReturn(Optional.of(agendamentoMock));

    Assertions.assertDoesNotThrow(() ->
      agendamentoService.cancelar(1L, "Cliente desistiu")
    );
    verify(agendamentoMock).cancelar("Cliente desistiu");
  }

  @Test
  @DisplayName(
    "Deveria lançar erro ao tentar cancelar agendamento com menos de 24h de antecedência"
  )
  void cenarioCancelarErroAntecedencia() {
    var agendamentoMock = mock(Agendamento.class);
    when(agendamentoMock.getDataHora()).thenReturn(
      LocalDateTime.now().plusHours(2)
    );
    when(agendamentoMock.getStatus()).thenReturn(StatusAgendamento.AGENDADO);
    when(repository.findById(1L)).thenReturn(Optional.of(agendamentoMock));

    var excecao = Assertions.assertThrows(ValidacaoException.class, () ->
      agendamentoService.cancelar(1L, "Mudança de planos")
    );
    Assertions.assertEquals(
      "Cancelamento exige 24h de antecedência.",
      excecao.getMessage()
    );
  }

  @Test
  @DisplayName("Deveria atualizar um agendamento com sucesso")
  void cenarioAtualizarAgendamentoComSucesso() {
    var novaData = LocalDateTime.now()
      .plusWeeks(1)
      .with(DayOfWeek.TUESDAY)
      .withHour(14)
      .withMinute(0);

    var dados = new AtualizarAgendamentoDTO(2L, 3L, 4L, List.of(5L), novaData);

    var agendamento = mock(Agendamento.class);
    var cliente = mock(Cliente.class);
    var aluno = mock(Aluno.class);
    var curso = mock(Curso.class);
    var unidade = mock(Unidade.class);
    var servico = mock(Servico.class);
    var usuarioCliente = mock(Usuario.class);
    var usuarioAluno = mock(Usuario.class);

    when(repository.findById(1L)).thenReturn(Optional.of(agendamento));
    when(agendamento.getStatus()).thenReturn(StatusAgendamento.AGENDADO);
    when(agendamento.getId()).thenReturn(1L);
    when(agendamento.getCliente()).thenReturn(cliente);
    when(cliente.getId()).thenReturn(1L);
    when(cliente.getUsuario()).thenReturn(usuarioCliente);
    when(usuarioCliente.getNome()).thenReturn("Cliente Teste");

    when(alunoService.obterEntidadePorIdECurso(2L, 3L)).thenReturn(aluno);
    when(cursoRepository.findByIdAndAtivoTrue(3L)).thenReturn(
      Optional.of(curso)
    );
    when(curso.getId()).thenReturn(3L);
    when(curso.getNome()).thenReturn("Curso Teste");
    when(aluno.getId()).thenReturn(2L);
    when(aluno.getUsuario()).thenReturn(usuarioAluno);
    when(usuarioAluno.getNome()).thenReturn("Aluno Teste");

    when(unidadeRepository.findByIdAndAtivoTrue(4L)).thenReturn(
      Optional.of(unidade)
    );
    when(unidade.getNome()).thenReturn("Unidade Teste");

    when(servicoService.buscarServicosValidos(List.of(5L))).thenReturn(
      List.of(servico)
    );
    when(servico.getValor()).thenReturn(new BigDecimal("100.00"));

    when(agendamento.getAluno()).thenReturn(aluno);
    when(agendamento.getCurso()).thenReturn(curso);
    when(agendamento.getUnidade()).thenReturn(unidade);
    when(agendamento.getDataHora()).thenReturn(novaData);
    when(agendamento.getValorNoAto()).thenReturn(new BigDecimal("100.00"));

    var resultado = agendamentoService.atualizar(1L, dados);

    assertNotNull(resultado);
    verify(agendamento).atualizar(
      aluno,
      curso,
      unidade,
      List.of(servico),
      novaData,
      new BigDecimal("100.00")
    );
  }

  @Test
  @DisplayName("Deveria recusar atualização sem nenhum campo")
  void cenarioAtualizarSemAlteracoes() {
    var dados = new AtualizarAgendamentoDTO(null, null, null, null, null);

    var erro = assertThrows(ValidacaoException.class, () ->
      agendamentoService.atualizar(1L, dados)
    );

    assertEquals(
      "Informe pelo menos um campo para atualizar o agendamento.",
      erro.getMessage()
    );
    verifyNoInteractions(repository);
  }

  @Test
  @DisplayName("Deveria recusar alteração de agendamento cancelado")
  void cenarioAtualizarAgendamentoCancelado() {
    var agendamento = mock(Agendamento.class);
    var dados = new AtualizarAgendamentoDTO(
      null,
      null,
      null,
      null,
      LocalDateTime.now().plusDays(2)
    );

    when(repository.findById(1L)).thenReturn(Optional.of(agendamento));
    when(agendamento.getStatus()).thenReturn(StatusAgendamento.CANCELADO);

    var erro = assertThrows(ValidacaoException.class, () ->
      agendamentoService.atualizar(1L, dados)
    );

    assertEquals(
      "Somente agendamentos com status AGENDADO podem ser alterados.",
      erro.getMessage()
    );
    verify(agendamento, never()).atualizar(
      any(),
      any(),
      any(),
      anyList(),
      any(),
      any()
    );
  }

  @Test
  @DisplayName("Deveria recusar aluno que não pertence ao curso")
  void cenarioAtualizarComAlunoDeOutroCurso() {
    var agendamento = mock(Agendamento.class);
    var cursoSelecionado = mock(Curso.class);

    var dados = new AtualizarAgendamentoDTO(2L, 3L, null, null, null);

    when(repository.findById(1L)).thenReturn(Optional.of(agendamento));
    when(agendamento.getStatus()).thenReturn(StatusAgendamento.AGENDADO);
    when(cursoRepository.findByIdAndAtivoTrue(3L)).thenReturn(
      Optional.of(cursoSelecionado)
    );
    when(cursoSelecionado.getId()).thenReturn(3L);
    when(alunoService.obterEntidadePorIdECurso(2L, 3L)).thenThrow(
      new ValidacaoException(
        "O aluno informado não pertence ao curso selecionado."
      )
    );

    var erro = assertThrows(ValidacaoException.class, () ->
      agendamentoService.atualizar(1L, dados)
    );

    assertEquals(
      "O aluno informado não pertence ao curso selecionado.",
      erro.getMessage()
    );
  }

  @Test
  @DisplayName("Deveria ignorar o próprio ID ao validar conflito")
  void cenarioAtualizarComConflitoDeAluno() {
    var novaData = LocalDateTime.now()
      .plusWeeks(1)
      .with(DayOfWeek.TUESDAY)
      .withHour(14)
      .withMinute(0);

    var agendamento = mock(Agendamento.class);
    var cliente = mock(Cliente.class);
    var aluno = mock(Aluno.class);
    var curso = mock(Curso.class);

    var dados = new AtualizarAgendamentoDTO(null, null, null, null, novaData);

    when(repository.findById(1L)).thenReturn(Optional.of(agendamento));
    when(agendamento.getStatus()).thenReturn(StatusAgendamento.AGENDADO);
    when(agendamento.getId()).thenReturn(1L);
    when(agendamento.getCliente()).thenReturn(cliente);
    when(cliente.getId()).thenReturn(1L);
    when(agendamento.getAluno()).thenReturn(aluno);
    when(aluno.getId()).thenReturn(2L);
    when(agendamento.getCurso()).thenReturn(curso);
    when(agendamento.getServicos()).thenReturn(List.of());

    when(
      repository.existsByAlunoIdAndDataHoraAndAtivoTrueAndIdNot(
        2L,
        novaData,
        1L
      )
    ).thenReturn(true);

    var erro = assertThrows(ValidacaoException.class, () ->
      agendamentoService.atualizar(1L, dados)
    );

    assertEquals(
      "O aluno já possui agendamento neste horário.",
      erro.getMessage()
    );
  }

  @Test
  @DisplayName("Deveria concluir agendamento realizado")
  void cenarioConcluirAgendamentoComSucesso() {
    var agendamento = mock(Agendamento.class);

    when(repository.findById(1L)).thenReturn(Optional.of(agendamento));
    when(agendamento.getStatus()).thenReturn(StatusAgendamento.AGENDADO);
    when(agendamento.getDataHora()).thenReturn(
      LocalDateTime.now().minusHours(1)
    );

    agendamentoService.concluir(1L);

    verify(agendamento).concluir();
  }

  @Test
  @DisplayName("Deveria recusar conclusão antes do horário marcado")
  void cenarioConcluirAntesDoHorario() {
    var agendamento = mock(Agendamento.class);

    when(repository.findById(1L)).thenReturn(Optional.of(agendamento));
    when(agendamento.getStatus()).thenReturn(StatusAgendamento.AGENDADO);
    when(agendamento.getDataHora()).thenReturn(
      LocalDateTime.now().plusHours(1)
    );

    var erro = assertThrows(ValidacaoException.class, () ->
      agendamentoService.concluir(1L)
    );

    assertEquals(
      "Não é possível concluir um agendamento antes do horário marcado.",
      erro.getMessage()
    );
    verify(agendamento, never()).concluir();
  }

  @Test
  @DisplayName("Deveria recusar conclusão de agendamento cancelado")
  void cenarioConcluirAgendamentoCancelado() {
    var agendamento = mock(Agendamento.class);

    when(repository.findById(1L)).thenReturn(Optional.of(agendamento));
    when(agendamento.getStatus()).thenReturn(StatusAgendamento.CANCELADO);

    var erro = assertThrows(ValidacaoException.class, () ->
      agendamentoService.concluir(1L)
    );

    assertEquals(
      "Somente agendamentos com status AGENDADO podem ser concluídos.",
      erro.getMessage()
    );
    verify(agendamento, never()).concluir();
  }

  @Test
  @DisplayName("Deveria impedir cliente de concluir agendamento")
  void cenarioClienteNaoPodeConcluirAgendamento() {
    doReturn(List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")))
      .when(usuarioLogado)
      .getAuthorities();

    var agendamento = mock(Agendamento.class);
    var aluno = mock(Aluno.class);

    when(repository.findById(1L)).thenReturn(Optional.of(agendamento));
    when(agendamento.getAluno()).thenReturn(aluno);
    when(aluno.getId()).thenReturn(2L);

    assertThrows(AccessDeniedException.class, () ->
      agendamentoService.concluir(1L)
    );

    verify(agendamento, never()).concluir();
  }

  @Test
  @DisplayName("Deveria listar agendamentos dos perfis cliente e aluno")
  void cenarioListarParaUsuarioComMultiplosPerfis() {
    doReturn(
      List.of(
        new SimpleGrantedAuthority("ROLE_CLIENTE"),
        new SimpleGrantedAuthority("ROLE_ALUNO")
      )
    )
      .when(usuarioLogado)
      .getAuthorities();

    var paginacao = Pageable.unpaged();

    when(repository.findAllVinculadosAoUsuario(1L, null, paginacao)).thenReturn(
      Page.empty(paginacao)
    );

    var resultado = agendamentoService.listar(paginacao, null);

    assertTrue(resultado.isEmpty());
    verify(repository).findAllVinculadosAoUsuario(1L, null, paginacao);
    verify(repository, never()).findAll(paginacao);
  }

  @Test
  @DisplayName("Deveria permitir que funcionário liste todos os agendamentos")
  void cenarioListarTodosParaFuncionario() {
    var paginacao = Pageable.unpaged();

    when(repository.findAll(paginacao)).thenReturn(Page.empty(paginacao));

    var resultado = agendamentoService.listar(paginacao, null);

    assertTrue(resultado.isEmpty());
    verify(repository).findAll(paginacao);
    verify(repository, never()).findAllVinculadosAoUsuario(any(), any(), any());
  }

  @Test
  @DisplayName("Deveria recusar listagem para usuário sem perfil permitido")
  void cenarioListarSemPerfilPermitido() {
    doReturn(List.of()).when(usuarioLogado).getAuthorities();

    assertThrows(AccessDeniedException.class, () ->
      agendamentoService.listar(Pageable.unpaged(), null)
    );

    verifyNoInteractions(repository);
  }

  @Test
  @DisplayName("Deveria filtrar agendamentos por status para funcionário")
  void cenarioFiltrarPorStatusParaFuncionario() {
    var paginacao = Pageable.unpaged();

    when(
      repository.findAllByStatus(StatusAgendamento.CONCLUIDO, paginacao)
    ).thenReturn(Page.empty(paginacao));

    var resultado = agendamentoService.listar(
      paginacao,
      StatusAgendamento.CONCLUIDO
    );

    assertTrue(resultado.isEmpty());
    verify(repository).findAllByStatus(StatusAgendamento.CONCLUIDO, paginacao);
    verify(repository, never()).findAll(paginacao);
  }

  @Test
  @DisplayName("Deveria filtrar apenas agendamentos vinculados ao usuário")
  void cenarioFiltrarPorStatusParaCliente() {
    doReturn(List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")))
      .when(usuarioLogado)
      .getAuthorities();

    var paginacao = Pageable.unpaged();

    when(
      repository.findAllVinculadosAoUsuario(
        1L,
        StatusAgendamento.AGENDADO,
        paginacao
      )
    ).thenReturn(Page.empty(paginacao));

    var resultado = agendamentoService.listar(
      paginacao,
      StatusAgendamento.AGENDADO
    );

    assertTrue(resultado.isEmpty());
    verify(repository).findAllVinculadosAoUsuario(
      1L,
      StatusAgendamento.AGENDADO,
      paginacao
    );
  }

  @Test
  @DisplayName("Deveria permitir que o cliente cancele o próprio agendamento")
  void cenarioClienteCancelaProprioAgendamento() {
    doReturn(List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")))
      .when(usuarioLogado)
      .getAuthorities();

    var agendamento = mock(Agendamento.class);
    var cliente = mock(Cliente.class);

    when(repository.findById(1L)).thenReturn(Optional.of(agendamento));
    when(agendamento.getCliente()).thenReturn(cliente);
    when(cliente.getId()).thenReturn(1L);
    when(agendamento.getStatus()).thenReturn(StatusAgendamento.AGENDADO);
    when(agendamento.getDataHora()).thenReturn(LocalDateTime.now().plusDays(3));

    agendamentoService.cancelar(1L, "Mudança de planos");

    verify(agendamento).cancelar("Mudança de planos");
  }

  @Test
  @DisplayName("Deveria impedir aluno de cancelar agendamento")
  void cenarioAlunoNaoPodeCancelarAgendamento() {
    doReturn(List.of(new SimpleGrantedAuthority("ROLE_ALUNO")))
      .when(usuarioLogado)
      .getAuthorities();

    var agendamento = mock(Agendamento.class);
    var cliente = mock(Cliente.class);

    when(repository.findById(1L)).thenReturn(Optional.of(agendamento));
    when(agendamento.getCliente()).thenReturn(cliente);
    when(cliente.getId()).thenReturn(2L);

    assertThrows(AccessDeniedException.class, () ->
      agendamentoService.cancelar(1L, "Cancelamento indevido")
    );

    verify(agendamento, never()).cancelar(any());
  }

  @Test
  @DisplayName("Deveria impedir o cancelamento repetido")
  void cenarioCancelarAgendamentoJaCancelado() {
    var agendamento = mock(Agendamento.class);

    when(repository.findById(1L)).thenReturn(Optional.of(agendamento));
    when(agendamento.getStatus()).thenReturn(StatusAgendamento.CANCELADO);

    var erro = assertThrows(ValidacaoException.class, () ->
      agendamentoService.cancelar(1L, "Novo cancelamento")
    );

    assertEquals(
      "Somente agendamentos com status AGENDADO podem ser cancelados.",
      erro.getMessage()
    );
    verify(agendamento, never()).cancelar(any());
  }

  @Test
  @DisplayName("Deveria recusar agendamento inexistente")
  void cenarioDetalharAgendamentoInexistente() {
    when(repository.findById(999L)).thenReturn(Optional.empty());

    var erro = assertThrows(RecursoNaoEncontradoException.class, () ->
      agendamentoService.detalhar(999L)
    );

    assertEquals("Agendamento não encontrado.", erro.getMessage());
  }

  @Test
  @DisplayName("Deveria selecionar automaticamente um aluno disponível")
  void cenarioAgendamentoComAlunoAutomatico() {
    var dataHora = LocalDateTime.now()
      .plusWeeks(1)
      .with(DayOfWeek.TUESDAY)
      .withHour(14)
      .withMinute(0);

    var dados = new CadastrarAgendamentoDTO(
      1L,
      null,
      1L,
      1L,
      List.of(1L),
      dataHora
    );

    var usuario = mock(Usuario.class);
    var cliente = mock(Cliente.class);
    var aluno = mock(Aluno.class);
    var curso = mock(Curso.class);
    var unidade = mock(Unidade.class);
    var servico = mock(Servico.class);

    when(usuario.getNome()).thenReturn("Usuário Teste");
    when(cliente.getUsuario()).thenReturn(usuario);
    when(aluno.getUsuario()).thenReturn(usuario);
    when(servico.getValor()).thenReturn(new BigDecimal("100.00"));

    when(
      clienteRepository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(1L)
    ).thenReturn(Optional.of(cliente));

    when(cursoRepository.findByIdAndAtivoTrue(1L)).thenReturn(
      Optional.of(curso)
    );

    when(alunoService.buscarAlunoAleatorio(1L, dataHora)).thenReturn(2L);

    when(alunoService.obterEntidadePorIdECurso(2L, 1L)).thenReturn(aluno);

    when(unidadeRepository.findByIdAndAtivoTrue(1L)).thenReturn(
      Optional.of(unidade)
    );

    when(servicoService.buscarServicosValidos(List.of(1L))).thenReturn(
      List.of(servico)
    );

    var resultado = agendamentoService.agendar(dados);

    assertNotNull(resultado);
    verify(alunoService).buscarAlunoAleatorio(1L, dataHora);
    verify(alunoService).obterEntidadePorIdECurso(2L, 1L);
    verify(repository).save(any(Agendamento.class));
  }
}
