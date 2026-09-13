package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.RecursoNaoEncontradoException;
import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.agendamento.Agendamento;
import com.tcc.uscs.model.agendamento.StatusAgendamento;
import com.tcc.uscs.model.agendamento.dto.AtualizarAgendamentoDTO;
import com.tcc.uscs.model.agendamento.dto.CadastrarAgendamentoDTO;
import com.tcc.uscs.model.agendamento.dto.DetalharAgendamentoDTO;
import com.tcc.uscs.model.agendamento.dto.ListarAgendamentoDTO;
import com.tcc.uscs.model.servico.Servico;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.*;
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

  @Transactional(readOnly = true)
  public Page<ListarAgendamentoDTO> listar(
    Pageable paginacao,
    StatusAgendamento status
  ) {
    var usuario = getUsuarioAutenticado();

    boolean isFuncionario = usuario
      .getAuthorities()
      .stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_FUNCIONARIO"));

    if (isFuncionario) {
      var pagina =
        status == null
          ? repository.findAll(paginacao)
          : repository.findAllByStatus(status, paginacao);

      return pagina.map(ListarAgendamentoDTO::new);
    }

    boolean possuiPerfilClienteOuAluno = usuario
      .getAuthorities()
      .stream()
      .anyMatch(
        a ->
          a.getAuthority().equals("ROLE_CLIENTE") ||
          a.getAuthority().equals("ROLE_ALUNO")
      );

    if (possuiPerfilClienteOuAluno) {
      return repository
        .findAllVinculadosAoUsuario(usuario.getId(), status, paginacao)
        .map(ListarAgendamentoDTO::new);
    }

    throw new AccessDeniedException(
      "Usuário sem perfil autorizado para consultar agendamentos."
    );
  }

  @Transactional(readOnly = true)
  public DetalharAgendamentoDTO detalhar(Long id) {
    var agendamento = obterAgendamento(id);
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

    var cliente = clienteRepository
      .findByIdAndAtivoTrueAndUsuarioAtivoTrue(dados.idCliente())
      .orElseThrow(() ->
        new RecursoNaoEncontradoException("Cliente não encontrado ou inativo!")
      );

    var curso = cursoRepository
      .findByIdAndAtivoTrue(dados.idCurso())
      .orElseThrow(() ->
        new RecursoNaoEncontradoException("Curso não encontrado ou inativo!")
      );

    Long idAluno = dados.idAluno();

    if (idAluno == null) {
      idAluno = alunoService.buscarAlunoAleatorio(
        dados.idCurso(),
        dados.dataHora()
      );
    }

    var aluno = alunoService.obterEntidadePorIdECurso(idAluno, dados.idCurso());
    var unidade = unidadeRepository
      .findByIdAndAtivoTrue(dados.idUnidade())
      .orElseThrow(() ->
        new RecursoNaoEncontradoException(
          "Unidade/Franquia não encontrada ou inativa!"
        )
      );

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

    BigDecimal valorTotalServicos = calcularValorTotal(servicosSelecionados);

    agendamento.setValorNoAto(valorTotalServicos);
    repository.save(agendamento);

    return new DetalharAgendamentoDTO(agendamento);
  }

  @Transactional
  public DetalharAgendamentoDTO atualizar(
    Long id,
    AtualizarAgendamentoDTO dados
  ) {
    if (dados.semAlteracoes()) {
      throw new ValidacaoException(
        "Informe pelo menos um campo para atualizar o agendamento."
      );
    }

    var agendamento = obterAgendamento(id);

    validarPermissaoParaAlterarOuCancelar(agendamento);

    if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
      throw new ValidacaoException(
        "Somente agendamentos com status AGENDADO podem ser alterados."
      );
    }

    var curso =
      dados.idCurso() != null
        ? cursoRepository
            .findByIdAndAtivoTrue(dados.idCurso())
            .orElseThrow(() ->
              new RecursoNaoEncontradoException(
                "Curso não encontrado ou inativo!"
              )
            )
        : agendamento.getCurso();
    var aluno =
      dados.idAluno() != null || dados.idCurso() != null
        ? alunoService.obterEntidadePorIdECurso(
            dados.idAluno() != null
              ? dados.idAluno()
              : agendamento.getAluno().getId(),
            curso.getId()
          )
        : agendamento.getAluno();

    var unidade =
      dados.idUnidade() != null
        ? unidadeRepository
            .findByIdAndAtivoTrue(dados.idUnidade())
            .orElseThrow(() ->
              new ValidacaoException(
                "Unidade/Franquia não encontrada ou inativa!"
              )
            )
        : agendamento.getUnidade();

    var servicos =
      dados.idServicos() != null
        ? servicoService.buscarServicosValidos(dados.idServicos())
        : agendamento.getServicos();

    var dataHora =
      dados.dataHora() != null ? dados.dataHora() : agendamento.getDataHora();

    validarHorarioAntecedencia(dataHora);
    validarHorarioComercial(dataHora);
    validarConflitoHorarioNaAtualizacao(
      aluno.getId(),
      agendamento.getCliente().getId(),
      dataHora,
      agendamento.getId()
    );

    var valorTotal = calcularValorTotal(servicos);

    agendamento.atualizar(
      aluno,
      curso,
      unidade,
      servicos,
      dataHora,
      valorTotal
    );

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
    var agendamento = obterAgendamento(id);

    validarPermissaoParaAlterarOuCancelar(agendamento);

    if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
      throw new ValidacaoException(
        "Somente agendamentos com status AGENDADO podem ser cancelados."
      );
    }

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

  @Transactional
  public void concluir(Long id) {
    var agendamento = obterAgendamento(id);

    validarPermissaoParaConcluir(agendamento);

    if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
      throw new ValidacaoException(
        "Somente agendamentos com status AGENDADO podem ser concluídos."
      );
    }

    if (agendamento.getDataHora().isAfter(LocalDateTime.now())) {
      throw new ValidacaoException(
        "Não é possível concluir um agendamento antes do horário marcado."
      );
    }

    agendamento.concluir();
  }

  private void validarConflitoHorarioNaAtualizacao(
    Long idAluno,
    Long idCliente,
    LocalDateTime data,
    Long idAgendamento
  ) {
    if (
      repository.existsByAlunoIdAndDataHoraAndAtivoTrueAndIdNot(
        idAluno,
        data,
        idAgendamento
      )
    ) {
      throw new ValidacaoException(
        "O aluno já possui agendamento neste horário."
      );
    }

    if (
      repository.existsByClienteIdAndDataHoraAndAtivoTrueAndIdNot(
        idCliente,
        data,
        idAgendamento
      )
    ) {
      throw new ValidacaoException(
        "O cliente já possui agendamento neste horário."
      );
    }
  }

  private BigDecimal calcularValorTotal(List<Servico> servicos) {
    return servicos
      .stream()
      .map(Servico::getValor)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private void validarPermissaoParaAlterarOuCancelar(Agendamento agendamento) {
    var usuario = getUsuarioAutenticado();

    boolean isFuncionario = usuario
      .getAuthorities()
      .stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_FUNCIONARIO"));

    if (isFuncionario) {
      return;
    }

    boolean isClienteDono =
      agendamento.getCliente() != null &&
      agendamento.getCliente().getId().equals(usuario.getId());

    if (!isClienteDono) {
      throw new AccessDeniedException(
        "Você não tem permissão para alterar ou cancelar este agendamento."
      );
    }
  }

  private void validarPermissaoParaConcluir(Agendamento agendamento) {
    var usuario = getUsuarioAutenticado();

    boolean isFuncionario = usuario
      .getAuthorities()
      .stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_FUNCIONARIO"));

    if (isFuncionario) {
      return;
    }

    boolean isAlunoResponsavel =
      agendamento.getAluno() != null &&
      agendamento.getAluno().getId().equals(usuario.getId());

    if (!isAlunoResponsavel) {
      throw new AccessDeniedException(
        "Você não tem permissão para concluir este agendamento."
      );
    }
  }

  private Agendamento obterAgendamento(Long id) {
    return repository
      .findById(id)
      .orElseThrow(() ->
        new RecursoNaoEncontradoException("Agendamento não encontrado.")
      );
  }
}
