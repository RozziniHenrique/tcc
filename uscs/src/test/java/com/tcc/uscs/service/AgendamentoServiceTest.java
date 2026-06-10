package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.agendamento.Agendamento;
import com.tcc.uscs.model.agendamento.dto.CadastrarAgendamentoDTO;
import com.tcc.uscs.model.servico.Servico;
import com.tcc.uscs.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    org.mockito.Mockito.when(clienteRepository.existsById(1L)).thenReturn(true);
    org.mockito.Mockito.when(unidadeRepository.existsById(1L)).thenReturn(true);

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
    var domingo = LocalDateTime.of(2026, 6, 14, 10, 0);

    var dto = new CadastrarAgendamentoDTO(1L, 1L, 1L, 1L, List.of(1L), domingo);

    org.mockito.Mockito.when(clienteRepository.existsById(1L)).thenReturn(true);
    org.mockito.Mockito.when(unidadeRepository.existsById(1L)).thenReturn(true);

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
    var dataComercialValida = LocalDateTime.of(2026, 6, 16, 14, 0);

    var dto = new CadastrarAgendamentoDTO(
      1L,
      1L,
      1L,
      1L,
      List.of(1L),
      dataComercialValida
    );

    org.mockito.Mockito.when(clienteRepository.existsById(1L)).thenReturn(true);
    org.mockito.Mockito.when(unidadeRepository.existsById(1L)).thenReturn(true);

    org.mockito.Mockito.when(
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
    var dataComercialValida = LocalDateTime.of(2026, 6, 16, 14, 0);
    var dto = new CadastrarAgendamentoDTO(
      1L,
      1L,
      1L,
      1L,
      List.of(1L),
      dataComercialValida
    );

    org.mockito.Mockito.when(clienteRepository.existsById(1L)).thenReturn(true);
    org.mockito.Mockito.when(unidadeRepository.existsById(1L)).thenReturn(true);

    org.mockito.Mockito.when(
      repository.existsByAlunoIdAndDataHoraAndAtivoTrue(1L, dataComercialValida)
    ).thenReturn(false);
    org.mockito.Mockito.when(
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
    var dataValida = LocalDateTime.of(2026, 6, 16, 14, 0);
    var dto = new CadastrarAgendamentoDTO(
      1L,
      1L,
      1L,
      1L,
      List.of(1L, 2L),
      dataValida
    );

    org.mockito.Mockito.when(clienteRepository.existsById(1L)).thenReturn(true);
    org.mockito.Mockito.when(unidadeRepository.existsById(1L)).thenReturn(true);

    var usuarioFake = org.mockito.Mockito.mock(
      com.tcc.uscs.model.usuario.Usuario.class
    );
    org.mockito.Mockito.when(usuarioFake.getNome()).thenReturn("Nome de Teste");

    var clienteMock = org.mockito.Mockito.mock(
      com.tcc.uscs.model.cliente.Cliente.class
    );
    org.mockito.Mockito.when(clienteMock.getUsuario()).thenReturn(usuarioFake);

    var alunoMock = org.mockito.Mockito.mock(
      com.tcc.uscs.model.aluno.Aluno.class
    );
    org.mockito.Mockito.when(alunoMock.getUsuario()).thenReturn(usuarioFake);

    var cursoMock = org.mockito.Mockito.mock(
      com.tcc.uscs.model.curso.Curso.class
    );
    var unidadeMock = org.mockito.Mockito.mock(
      com.tcc.uscs.model.unidade.Unidade.class
    );

    org.mockito.Mockito.when(clienteRepository.getReferenceById(1L)).thenReturn(
      clienteMock
    );
    org.mockito.Mockito.when(alunoService.obterReferencia(1L)).thenReturn(
      alunoMock
    );
    org.mockito.Mockito.when(cursoRepository.getReferenceById(1L)).thenReturn(
      cursoMock
    );
    org.mockito.Mockito.when(unidadeRepository.getReferenceById(1L)).thenReturn(
      unidadeMock
    );

    var s1 = org.mockito.Mockito.mock(Servico.class);
    org.mockito.Mockito.when(s1.getValor()).thenReturn(
      new BigDecimal("100.00")
    );

    var s2 = org.mockito.Mockito.mock(Servico.class);
    org.mockito.Mockito.when(s2.getValor()).thenReturn(new BigDecimal("50.00"));

    org.mockito.Mockito.when(
      servicoService.buscarServicosValidos(dto.idServicos())
    ).thenReturn(List.of(s1, s2));

    var resultado = agendamentoService.agendar(dto);

    org.mockito.Mockito.verify(repository).save(
      org.mockito.Mockito.any(Agendamento.class)
    );
    Assertions.assertNotNull(resultado);
  }

  @Test
  @DisplayName(
    "Deveria cancelar um agendamento com sucesso se tiver mais de 24h de antecedência"
  )
  void cenarioCancelarComSucesso() {
    var agendamentoMock = org.mockito.Mockito.mock(Agendamento.class);
    org.mockito.Mockito.when(agendamentoMock.getDataHora()).thenReturn(
      LocalDateTime.now().plusDays(3)
    );
    org.mockito.Mockito.when(repository.getReferenceById(1L)).thenReturn(
      agendamentoMock
    );

    Assertions.assertDoesNotThrow(() ->
      agendamentoService.cancelar(1L, "Cliente desistiu")
    );
    org.mockito.Mockito.verify(agendamentoMock).cancelar("Cliente desistiu");
  }

  @Test
  @DisplayName(
    "Deveria lançar erro ao tentar cancelar agendamento com menos de 24h de antecedência"
  )
  void cenarioCancelarErroAntecedencia() {
    var agendamentoMock = org.mockito.Mockito.mock(Agendamento.class);
    org.mockito.Mockito.when(agendamentoMock.getDataHora()).thenReturn(
      LocalDateTime.now().plusHours(2)
    );
    org.mockito.Mockito.when(repository.getReferenceById(1L)).thenReturn(
      agendamentoMock
    );

    var excecao = Assertions.assertThrows(ValidacaoException.class, () ->
      agendamentoService.cancelar(1L, "Mudança de planos")
    );
    Assertions.assertEquals(
      "Cancelamento exige 24h de antecedência.",
      excecao.getMessage()
    );
  }

  @Test
  @DisplayName(
    "Deveria lançar erro ao tentar cancelar sem enviar uma justificativa válida"
  )
  void cenarioCancelarErroJustificativaEmBranco() {
    var agendamentoMock = org.mockito.Mockito.mock(Agendamento.class);
    org.mockito.Mockito.when(agendamentoMock.getDataHora()).thenReturn(
      LocalDateTime.now().plusDays(2)
    );
    org.mockito.Mockito.when(repository.getReferenceById(1L)).thenReturn(
      agendamentoMock
    );

    var excecao = Assertions.assertThrows(ValidacaoException.class, () ->
      agendamentoService.cancelar(1L, "   ")
    );
    Assertions.assertEquals(
      "Justificativa é obrigatória.",
      excecao.getMessage()
    );
  }
}
