package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.agendamento.Agendamento;
import com.tcc.uscs.model.agendamento.dto.CadastrarAgendamentoDTO;
import com.tcc.uscs.model.agendamento.dto.DetalharAgendamentoDTO;
import com.tcc.uscs.repository.*;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AgendamentoService {

  private final AgendamentoRepository repository;

  private final ClienteRepository clienteRepository;

  private final AlunoRepository alunoRepository;

  private final CursoRepository cursoRepository;

  @Transactional
  public DetalharAgendamentoDTO agendar(CadastrarAgendamentoDTO dados) {
    // 1. Validações de Existência
    if (!clienteRepository.existsById(dados.idCliente())) {
      throw new ValidacaoException("Cliente não encontrado ou inativo!");
    }

    Long idAluno = dados.idAluno();
    if (idAluno == null) {
      idAluno = buscarAlunoAleatorio(dados.idCurso());
    } else if (!alunoRepository.existsById(idAluno)) {
      throw new ValidacaoException("Aluno não encontrado ou inativo!");
    }

    var cliente = clienteRepository.getReferenceById(dados.idCliente());
    var aluno = alunoRepository.getReferenceById(idAluno);
    var curso = cursoRepository.getReferenceById(dados.idCurso());

    // 2. Regras de Negócio
    validarHorarioAntecedencia(dados.dataHora());
    validarHorarioComercial(dados.dataHora());
    validarConflitoHorario(idAluno, dados.idCliente(), dados.dataHora());

    // 3. Persistência
    var agendamento = new Agendamento(cliente, aluno, curso, dados.dataHora());
    agendamento.setValorNoAto(curso.getValor());
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
    agendamento.cancelar();
  }
}
