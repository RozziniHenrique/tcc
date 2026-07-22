package com.tcc.uscs.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.agendamento.Agendamento;
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

    doReturn(List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO")))
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

    when(clienteRepository.findById(1L)).thenReturn(
      Optional.of(mock(Cliente.class))
    );
    when(alunoService.obterEntidadePorId(1L)).thenReturn(mock(Aluno.class));
    when(cursoRepository.findById(1L)).thenReturn(
      Optional.of(mock(Curso.class))
    );
    when(unidadeRepository.findById(1L)).thenReturn(
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

    when(clienteRepository.findById(1L)).thenReturn(
      Optional.of(mock(Cliente.class))
    );
    when(alunoService.obterEntidadePorId(1L)).thenReturn(mock(Aluno.class));
    when(cursoRepository.findById(1L)).thenReturn(
      Optional.of(mock(Curso.class))
    );
    when(unidadeRepository.findById(1L)).thenReturn(
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

    when(clienteRepository.findById(1L)).thenReturn(
      Optional.of(mock(Cliente.class))
    );
    when(alunoService.obterEntidadePorId(1L)).thenReturn(mock(Aluno.class));
    when(cursoRepository.findById(1L)).thenReturn(
      Optional.of(mock(Curso.class))
    );
    when(unidadeRepository.findById(1L)).thenReturn(
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

    when(clienteRepository.findById(1L)).thenReturn(
      Optional.of(mock(Cliente.class))
    );
    when(alunoService.obterEntidadePorId(1L)).thenReturn(mock(Aluno.class));
    when(cursoRepository.findById(1L)).thenReturn(
      Optional.of(mock(Curso.class))
    );
    when(unidadeRepository.findById(1L)).thenReturn(
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

    when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteMock));
    when(alunoService.obterEntidadePorId(1L)).thenReturn(alunoMock);
    when(cursoRepository.findById(1L)).thenReturn(Optional.of(cursoMock));
    when(unidadeRepository.findById(1L)).thenReturn(Optional.of(unidadeMock));

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
    when(repository.findById(1L)).thenReturn(Optional.of(agendamentoMock));

    var excecao = Assertions.assertThrows(ValidacaoException.class, () ->
      agendamentoService.cancelar(1L, "Mudança de planos")
    );
    Assertions.assertEquals(
      "Cancelamento exige 24h de antecedência.",
      excecao.getMessage()
    );
  }
}
