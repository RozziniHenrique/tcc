package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.agendamento.*;
import com.tcc.uscs.model.agendamento.dto.*;
import com.tcc.uscs.model.servico.Servico;
import com.tcc.uscs.repository.*;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AgendamentoService {

  private final AgendamentoRepository repository;
  private final ClienteRepository clienteRepository;
  private final AlunoRepository alunoRepository;
  private final CursoRepository cursoRepository;
  private final UnidadeRepository unidadeRepository;
  private final ServicoRepository servicoRepository;

  public Page<ListarAgendamentoDTO> listar(Pageable paginacao) {
    return repository.findAll(paginacao).map(ListarAgendamentoDTO::new);
  }

  public DetalharAgendamentoDTO detalhar(Long id) {
    return new DetalharAgendamentoDTO(repository.getReferenceById(id));
  }

  @Transactional
  public DetalharAgendamentoDTO agendar(CadastrarAgendamentoDTO dados) {
    if (!clienteRepository.existsById(dados.idCliente())) {
      throw new ValidacaoException("Cliente não encontrado ou inativo!");
    }

    if (!unidadeRepository.existsById(dados.idUnidade())) {
      throw new ValidacaoException(
        "Unidade/Franquia não encontrada ou inativa!"
      );
    }

    dados
      .idServicos()
      .forEach(idServico -> {
        if (!servicoRepository.existsById(idServico)) {
          throw new ValidacaoException(
            "Serviço com ID " + idServico + " não encontrado!"
          );
        }
      });

    Long idAluno = dados.idAluno();
    if (idAluno == null) {
      idAluno = buscarAlunoAleatorio(dados.idCurso());
    } else if (!alunoRepository.existsById(idAluno)) {
      throw new ValidacaoException("Aluno não encontrado ou inativo!");
    }

    var cliente = clienteRepository.getReferenceById(dados.idCliente());
    var aluno = alunoRepository.getReferenceById(idAluno);
    var curso = cursoRepository.getReferenceById(dados.idCurso());
    var unidade = unidadeRepository.getReferenceById(dados.idUnidade());

    List<Servico> servicosSelecionados = dados
      .idServicos()
      .stream()
      .map(servicoRepository::getReferenceById)
      .collect(Collectors.toList());

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

  private Long buscarAlunoAleatorio(Long idCurso) {
    var disponiveis = alunoRepository.findAllByCursoIdAndUsuarioAtivoTrue(
      idCurso
    );

    if (disponiveis.isEmpty()) {
      throw new ValidacaoException("Nenhum aluno disponível para este curso.");
    }
    int indiceAleatorio = ThreadLocalRandom.current().nextInt(
      disponiveis.size()
    );
    return disponiveis.get(indiceAleatorio).getId();
  }

  @Transactional
  public void cancelar(Long id, String justificativa) {
    var agendamento = repository.getReferenceById(id);

    if (
      Duration.between(
        LocalDateTime.now(),
        agendamento.getDataHora()
      ).toHours() <
      24
    ) {
      throw new ValidacaoException("Cancelamento exige 24h de antecedência.");
    }

    if (justificativa == null || justificativa.isBlank()) {
      throw new ValidacaoException("Justificativa é obrigatória.");
    }

    agendamento.cancelar(justificativa);
  }
}
