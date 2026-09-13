package com.tcc.uscs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tcc.uscs.infra.exception.RecursoNaoEncontradoException;
import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.agendamento.Agendamento;
import com.tcc.uscs.model.agendamento.StatusAgendamento;
import com.tcc.uscs.model.aluno.Aluno;
import com.tcc.uscs.model.avaliacao.Avaliacao;
import com.tcc.uscs.model.avaliacao.dto.AvaliacaoPendenteDTO;
import com.tcc.uscs.model.avaliacao.dto.CadastrarAvaliacaoDTO;
import com.tcc.uscs.model.cliente.Cliente;
import com.tcc.uscs.model.usuario.TipoUsuario;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.AgendamentoRepository;
import com.tcc.uscs.repository.AvaliacaoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

  @InjectMocks
  private AvaliacaoService avaliacaoService;

  @Mock
  private AvaliacaoRepository avaliacaoRepository;

  @Mock
  private AgendamentoRepository agendamentoRepository;

  @AfterEach
  void limparContexto() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void deveriaAvaliarAgendamentoConcluidoDoProprioCliente() {
    autenticarUsuario(1L);
    var agendamento = criarAgendamento(
      10L,
      1L,
      StatusAgendamento.CONCLUIDO,
      true
    );

    when(agendamentoRepository.findById(10L)).thenReturn(
      Optional.of(agendamento)
    );
    when(avaliacaoRepository.existsByAgendamentoId(10L)).thenReturn(false);

    var resultado = avaliacaoService.avaliar(
      new CadastrarAvaliacaoDTO(10L, 5, "Excelente atendimento")
    );

    var captor = ArgumentCaptor.forClass(Avaliacao.class);
    verify(avaliacaoRepository).save(captor.capture());

    var avaliacao = captor.getValue();

    assertEquals(agendamento, avaliacao.getAgendamento());
    assertEquals(5, avaliacao.getNota());
    assertEquals("Excelente atendimento", avaliacao.getComentario());
    assertNotNull(avaliacao.getDataAvaliacao());
    assertEquals(10L, resultado.idAgendamento());
  }

  @Test
  void deveriaRecusarAgendamentoInexistente() {
    when(agendamentoRepository.findById(10L)).thenReturn(Optional.empty());

    var erro = assertThrows(RecursoNaoEncontradoException.class, () ->
      avaliacaoService.avaliar(new CadastrarAvaliacaoDTO(10L, 5, null))
    );

    assertEquals("Agendamento não encontrado.", erro.getMessage());
    verifyNoInteractions(avaliacaoRepository);
  }

  @Test
  void deveriaRecusarAgendamentoDeOutroCliente() {
    autenticarUsuario(2L);
    var agendamento = criarAgendamento(
      10L,
      1L,
      StatusAgendamento.CONCLUIDO,
      true
    );

    when(agendamentoRepository.findById(10L)).thenReturn(
      Optional.of(agendamento)
    );

    var erro = assertThrows(AccessDeniedException.class, () ->
      avaliacaoService.avaliar(new CadastrarAvaliacaoDTO(10L, 5, null))
    );

    assertEquals(
      "Você só pode avaliar os seus próprios agendamentos.",
      erro.getMessage()
    );
    verifyNoInteractions(avaliacaoRepository);
  }

  @Test
  void deveriaRecusarAgendamentoAindaAgendado() {
    autenticarUsuario(1L);
    var agendamento = criarAgendamento(
      10L,
      1L,
      StatusAgendamento.AGENDADO,
      true
    );

    when(agendamentoRepository.findById(10L)).thenReturn(
      Optional.of(agendamento)
    );

    var erro = assertThrows(ValidacaoException.class, () ->
      avaliacaoService.avaliar(new CadastrarAvaliacaoDTO(10L, 5, null))
    );

    assertEquals(
      "Somente agendamentos concluídos podem ser avaliados.",
      erro.getMessage()
    );
    verifyNoInteractions(avaliacaoRepository);
  }

  @Test
  void deveriaRecusarAgendamentoCancelado() {
    autenticarUsuario(1L);
    var agendamento = criarAgendamento(
      10L,
      1L,
      StatusAgendamento.CANCELADO,
      false
    );

    when(agendamentoRepository.findById(10L)).thenReturn(
      Optional.of(agendamento)
    );

    assertThrows(ValidacaoException.class, () ->
      avaliacaoService.avaliar(new CadastrarAvaliacaoDTO(10L, 5, null))
    );

    verifyNoInteractions(avaliacaoRepository);
  }

  @Test
  void deveriaRecusarAvaliacaoDuplicada() {
    autenticarUsuario(1L);
    var agendamento = criarAgendamento(
      10L,
      1L,
      StatusAgendamento.CONCLUIDO,
      true
    );

    when(agendamentoRepository.findById(10L)).thenReturn(
      Optional.of(agendamento)
    );
    when(avaliacaoRepository.existsByAgendamentoId(10L)).thenReturn(true);

    var erro = assertThrows(ValidacaoException.class, () ->
      avaliacaoService.avaliar(new CadastrarAvaliacaoDTO(10L, 5, null))
    );

    assertEquals(
      "Este agendamento já foi avaliado anteriormente.",
      erro.getMessage()
    );
    verify(avaliacaoRepository, never()).save(any(Avaliacao.class));
  }

  private Usuario autenticarUsuario(Long idUsuario) {
    var usuario = mock(Usuario.class);
    var authentication = mock(Authentication.class);
    var securityContext = mock(SecurityContext.class);

    lenient().when(usuario.getId()).thenReturn(idUsuario);
    when(authentication.getPrincipal()).thenReturn(usuario);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    SecurityContextHolder.setContext(securityContext);

    return usuario;
  }

  private Agendamento criarAgendamento(
    Long idAgendamento,
    Long idCliente,
    StatusAgendamento status,
    boolean ativo
  ) {
    var agendamento = mock(Agendamento.class);
    var cliente = mock(Cliente.class);

    lenient().when(agendamento.getId()).thenReturn(idAgendamento);
    lenient().when(agendamento.getCliente()).thenReturn(cliente);
    lenient().when(cliente.getId()).thenReturn(idCliente);
    lenient().when(agendamento.getStatus()).thenReturn(status);
    lenient().when(agendamento.getAtivo()).thenReturn(ativo);

    return agendamento;
  }

  @Test
  void deveriaPermitirClienteConsultarPropriaAvaliacao() {
    autenticarUsuario(1L);
    var agendamento = criarAgendamento(
      10L,
      1L,
      StatusAgendamento.CONCLUIDO,
      true
    );
    var avaliacao = new Avaliacao(agendamento, 5, "Excelente");

    when(avaliacaoRepository.findByAgendamentoId(10L)).thenReturn(
      Optional.of(avaliacao)
    );

    var resultado = avaliacaoService.buscarPorAgendamento(10L);

    assertEquals(10L, resultado.idAgendamento());
    assertEquals(5, resultado.nota());
  }

  @Test
  void deveriaPermitirAlunoResponsavelConsultarAvaliacao() {
    autenticarUsuario(2L);
    var agendamento = criarAgendamento(
      10L,
      1L,
      StatusAgendamento.CONCLUIDO,
      true
    );
    var aluno = mock(Aluno.class);

    when(aluno.getId()).thenReturn(2L);
    when(agendamento.getAluno()).thenReturn(aluno);

    var avaliacao = new Avaliacao(agendamento, 5, "Excelente");

    when(avaliacaoRepository.findByAgendamentoId(10L)).thenReturn(
      Optional.of(avaliacao)
    );

    var resultado = avaliacaoService.buscarPorAgendamento(10L);

    assertEquals(10L, resultado.idAgendamento());
  }

  @Test
  void deveriaPermitirFuncionarioConsultarAvaliacao() {
    var usuario = autenticarUsuario(99L);
    var agendamento = criarAgendamento(
      10L,
      1L,
      StatusAgendamento.CONCLUIDO,
      true
    );
    var avaliacao = new Avaliacao(agendamento, 4, "Bom");

    when(usuario.possuiPerfil(TipoUsuario.FUNCIONARIO)).thenReturn(true);
    when(avaliacaoRepository.findByAgendamentoId(10L)).thenReturn(
      Optional.of(avaliacao)
    );

    var resultado = avaliacaoService.buscarPorAgendamento(10L);

    assertEquals(4, resultado.nota());
  }

  @Test
  void deveriaNegarConsultaDeAvaliacaoAlheia() {
    autenticarUsuario(99L);
    var agendamento = criarAgendamento(
      10L,
      1L,
      StatusAgendamento.CONCLUIDO,
      true
    );
    var avaliacao = new Avaliacao(agendamento, 5, null);

    when(avaliacaoRepository.findByAgendamentoId(10L)).thenReturn(
      Optional.of(avaliacao)
    );

    var erro = assertThrows(AccessDeniedException.class, () ->
      avaliacaoService.buscarPorAgendamento(10L)
    );

    assertEquals(
      "Você não tem permissão para consultar esta avaliação.",
      erro.getMessage()
    );
  }

  @Test
  void deveriaInformarQuandoAvaliacaoNaoExistir() {
    when(avaliacaoRepository.findByAgendamentoId(10L)).thenReturn(
      Optional.empty()
    );

    var erro = assertThrows(RecursoNaoEncontradoException.class, () ->
      avaliacaoService.buscarPorAgendamento(10L)
    );

    assertEquals(
      "Avaliação não encontrada para este agendamento.",
      erro.getMessage()
    );
  }

  @Test
  void deveriaListarAvaliacoesPendentesDoCliente() {
    var usuario = autenticarUsuario(1L);
    var pendentes = List.of(
      new AvaliacaoPendenteDTO(
        10L,
        2L,
        "Aluno Teste",
        3L,
        "Estética",
        LocalDateTime.of(2026, 1, 10, 14, 0)
      )
    );

    when(usuario.possuiPerfil(TipoUsuario.CLIENTE)).thenReturn(true);
    when(agendamentoRepository.listarPendentesDeAvaliacao(1L)).thenReturn(
      pendentes
    );

    var resultado = avaliacaoService.listarPendentes();

    assertEquals(pendentes, resultado);
    verify(agendamentoRepository).listarPendentesDeAvaliacao(1L);
  }

  @Test
  void deveriaNegarPendentesParaUsuarioQueNaoForCliente() {
    var usuario = autenticarUsuario(99L);

    when(usuario.possuiPerfil(TipoUsuario.CLIENTE)).thenReturn(false);

    var erro = assertThrows(AccessDeniedException.class, () ->
      avaliacaoService.listarPendentes()
    );

    assertEquals(
      "Somente clientes podem consultar avaliações pendentes.",
      erro.getMessage()
    );
    verifyNoInteractions(agendamentoRepository);
  }
}
