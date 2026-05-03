package com.tcc.uscs.service;

import com.tcc.uscs.model.agendamento.Agendamento;
import com.tcc.uscs.model.agendamento.dto.CadastrarAgendamentoDTO;
import com.tcc.uscs.model.agendamento.dto.DetalharAgendamentoDTO;
import com.tcc.uscs.repository.AgendamentoRepository;
import com.tcc.uscs.repository.AlunoRepository;
import com.tcc.uscs.repository.ClienteRepository;
import com.tcc.uscs.repository.CursoRepository;
import jakarta.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgendamentoService {

  @Autowired
  private AgendamentoRepository repository;

  @Autowired
  private ClienteRepository clienteRepository;

  @Autowired
  private AlunoRepository alunoRepository;

  @Autowired
  private CursoRepository cursoRepository;

  public DetalharAgendamentoDTO agendar(CadastrarAgendamentoDTO dados) {
    // 1. Validações de Existência
    if (!clienteRepository.existsById(dados.idCliente())) {
      throw new RuntimeException("Id do cliente informado não existe!");
    }

    // 2. Smart Booking: Se não informar aluno, busca um aleatório do curso
    Long idAluno = dados.idAluno();
    if (idAluno == null) {
      idAluno = buscarAlunoAleatorio(dados.idCurso());
    } else if (!alunoRepository.existsById(idAluno)) {
      throw new RuntimeException("Id do aluno informado não existe!");
    }

    var cliente = clienteRepository.getReferenceById(dados.idCliente());
    var aluno = alunoRepository.getReferenceById(idAluno);
    var curso = cursoRepository.getReferenceById(dados.idCurso());

    // 3. Validações de Regra de Negócio
    validarHorarioAntecedencia(dados.dataHora());
    validarHorarioComercial(dados.dataHora());
    validarConflitoHorario(idAluno, dados.idCliente(), dados.dataHora());

    // 4. Persistência com Valor no Ato
    var agendamento = new Agendamento(cliente, aluno, curso, dados.dataHora());
    agendamento.setValorNoAto(curso.getValor());
    repository.save(agendamento);

    return new DetalharAgendamentoDTO(agendamento);
  }

  private void validarHorarioAntecedencia(LocalDateTime data) {
    var agora = LocalDateTime.now();
    var diferencaEmMinutos = Duration.between(agora, data).toMinutes();
    if (diferencaEmMinutos < 30) {
      throw new RuntimeException(
        "Agendamento deve ter antecedência mínima de 30 minutos."
      );
    }
  }

  private void validarHorarioComercial(LocalDateTime data) {
    var domingo = data.getDayOfWeek().equals(DayOfWeek.SUNDAY);
    var antesDaAbertura = data.getHour() < 8;
    var depoisDoFechamento = data.getHour() > 18;

    if (domingo || antesDaAbertura || depoisDoFechamento) {
      throw new RuntimeException(
        "Agendamento fora do horário comercial (Seg-Sáb, 08:00 às 19:00)."
      );
    }
  }

  private void validarConflitoHorario(
    Long idAluno,
    Long idCliente,
    LocalDateTime data
  ) {
    var alunoOcupado = repository.existsByAlunoIdAndDataHoraAndAtivoTrue(
      idAluno,
      data
    );
    if (alunoOcupado) {
      throw new RuntimeException(
        "O aluno já possui um agendamento nesse horário!"
      );
    }

    var clienteOcupado = repository.existsByClienteIdAndDataHoraAndAtivoTrue(
      idCliente,
      data
    );
    if (clienteOcupado) {
      throw new RuntimeException(
        "O cliente já possui um agendamento nesse horário!"
      );
    }
  }

  private Long buscarAlunoAleatorio(Long idCurso) {
    var alunosDisponiveis = alunoRepository.findAllByCursoIdAndAtivoTrue(
      idCurso
    );
    if (alunosDisponiveis.isEmpty()) {
      throw new RuntimeException("Não há alunos disponíveis para este curso.");
    }
    return alunosDisponiveis
      .get(new Random().nextInt(alunosDisponiveis.size()))
      .getId();
  }

  @Transactional
  public void cancelar(Long id, String justificativa) {
    var agendamento = repository.getReferenceById(id);
    var agora = LocalDateTime.now();
    var diferencaEmHoras = Duration.between(
      agora,
      agendamento.getDataHora()
    ).toHours();

    if (diferencaEmHoras < 24) {
      throw new RuntimeException(
        "Cancelamento apenas com 24h de antecedência!"
      );
    }

    if (justificativa == null || justificativa.isBlank()) {
      throw new RuntimeException(
        "Justificativa obrigatória para cancelamento."
      );
    }

    agendamento.cancelar(); // Soft Delete
  }
}
