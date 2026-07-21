package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.agendamento.Agendamento;
import com.tcc.uscs.model.agendamento.dto.CadastrarAgendamentoDTO;
import com.tcc.uscs.model.agendamento.dto.DetalharAgendamentoDTO;
import com.tcc.uscs.model.agendamento.dto.ListarAgendamentoDTO;
import com.tcc.uscs.model.servico.Servico;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.AgendamentoRepository;
import com.tcc.uscs.repository.ClienteRepository;
import com.tcc.uscs.repository.CursoRepository;
import com.tcc.uscs.repository.UnidadeRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AgendamentoService {

  private final AgendamentoRepository repository;
  private final ClienteRepository clienteRepository;
  private final CursoRepository cursoRepository;
  private final UnidadeRepository unidadeRepository;
  private final AlunoService alunoService;
  private final ServicoService servicoService;

  public Page<ListarAgendamentoDTO> listar(Pageable paginacao) {
    var usuarioLogado = getUsuarioAutenticado();

    boolean isCliente = usuarioLogado
      .getAuthorities()
      .stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
    boolean isAluno = usuarioLogado
      .getAuthorities()
      .stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_ALUNO"));

    if (isCliente) {
      return repository
        .findAllByClienteIdAndAtivoTrue(usuarioLogado.getId(), paginacao)
        .map(ListarAgendamentoDTO::new);
    } else if (isAluno) {
      return repository
        .findAllByAlunoIdAndAtivoTrue(usuarioLogado.getId(), paginacao)
        .map(ListarAgendamentoDTO::new);
    }

    return repository
      .findAllByAtivoTrue(paginacao)
      .map(ListarAgendamentoDTO::new);
  }

  public DetalharAgendamentoDTO detalhar(Long id) {
    var agendamento = repository.getReferenceById(id);
    validarPosseDoAgendamento(agendamento);
    return new DetalharAgendamentoDTO(agendamento);
  }

  @Transactional
  public DetalharAgendamentoDTO agendar(CadastrarAgendamentoDTO dados) {
    var usuarioLogado = getUsuarioAutenticado();
    boolean isCliente = usuarioLogado
      .getAuthorities()
      .stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

    if (isCliente && !usuarioLogado.getId().equals(dados.idCliente())) {
      throw new AccessDeniedException(
        "Você só pode realizar agendamentos para si mesmo."
      );
    }

    if (!clienteRepository.existsById(dados.idCliente())) {
      throw new ValidacaoException("Cliente não encontrado ou inativo!");
    }

    if (!unidadeRepository.existsById(dados.idUnidade())) {
      throw new ValidacaoException(
        "Unidade/Franquia não encontrada ou inativa!"
      );
    }

    Long idAluno = dados.idAluno();
    if (idAluno == null) {
      idAluno = alunoService.buscarAlunoAleatorio(dados.idCurso());
    }

    var cliente = clienteRepository.getReferenceById(dados.idCliente());
    var aluno = alunoService.obterReferencia(idAluno);
    var curso = cursoRepository.getReferenceById(dados.idCurso());
    var unidade = unidadeRepository.getReferenceById(dados.idUnidade());

    List<Servico> servicosSelecionados = servicoService.buscarServicosValidos(
      dados.idServicos()
    );

    validarHorarioAntecedencia(dados.dataHora());
    validarHorarioComercial(dados.dataHora());
    validarConflitoHorario(idAluno, dados.idCliente(), dados.dataHora());

    var agendamento = new Agendamento(
      cliente,
      aluno,
      curso,
      unidade,
      dados.dataHora()
    );
    agendamento.setServicos(servicosSelecionados);

    BigDecimal valorTotalServicos = servicosSelecionados
      .stream()
      .map(Servico::getValor)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    agendamento.setValorNoAto(valorTotalServicos);
    repository.save(agendamento);

    return new DetalharAgendamentoDTO(agendamento);
  }

  private void validarHorarioAntecedencia(LocalDateTime data) {
    if (Duration.between(LocalDateTime.now(), data).toMinutes() < 30) {
      throw new ValidacaoException(
        "Antecedência mínima de 30 minutos exigida."
      );
    }
  }

  private void validarHorarioComercial(LocalDateTime data) {
    var domingo = data.getDayOfWeek().equals(DayOfWeek.SUNDAY);
    var foraHorario = data.getHour() < 8 || data.getHour() > 18;
    if (domingo || foraHorario) {
      throw new ValidacaoException(
        "Fora do horário comercial (Seg-Sáb, 08h-19h)."
      );
    }
  }

  private void validarConflitoHorario(
    Long idAluno,
    Long idCliente,
    LocalDateTime data
  ) {
    if (repository.existsByAlunoIdAndDataHoraAndAtivoTrue(idAluno, data)) {
      throw new ValidacaoException(
        "O aluno já possui agendamento neste horário."
      );
    }
    if (repository.existsByClienteIdAndDataHoraAndAtivoTrue(idCliente, data)) {
      throw new ValidacaoException(
        "O cliente já possui agendamento neste horário."
      );
    }
  }

  @Transactional
  public void cancelar(Long id, String justificativa) {
    var agendamento = repository.getReferenceById(id);
    validarPosseDoAgendamento(agendamento);

    if (
      Duration.between(
        LocalDateTime.now(),
        agendamento.getDataHora()
      ).toHours() <
      24
    ) {
      throw new ValidacaoException("Cancelamento exige 24h de antecedência.");
    }

    agendamento.cancelar(justificativa);
  }

  private Usuario getUsuarioAutenticado() {
    return (Usuario) SecurityContextHolder.getContext()
      .getAuthentication()
      .getPrincipal();
  }

  private void validarPosseDoAgendamento(Agendamento agendamento) {
    var usuarioLogado = getUsuarioAutenticado();
    boolean isFuncionario = usuarioLogado
      .getAuthorities()
      .stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_FUNCIONARIO"));

    if (!isFuncionario) {
      boolean isDonoCliente = agendamento
        .getCliente()
        .getId()
        .equals(usuarioLogado.getId());
      boolean isDonoAluno = agendamento
        .getAluno()
        .getId()
        .equals(usuarioLogado.getId());

      if (!isDonoCliente && !isDonoAluno) {
        throw new AccessDeniedException(
          "Você não tem permissão para interagir com este agendamento."
        );
      }
    }
  }
}
