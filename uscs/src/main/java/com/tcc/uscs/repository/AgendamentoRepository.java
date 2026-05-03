package com.tcc.uscs.repository;

import com.tcc.uscs.model.agendamento.Agendamento;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendamentoRepository
  extends JpaRepository<Agendamento, Long>
{
  Page<Agendamento> findAllByAtivoTrue(Pageable paginacao);

  boolean existsByAlunoIdAndDataHoraAndAtivoTrue(
    Long idAluno,
    LocalDateTime dataHora
  );
  boolean existsByClienteIdAndDataHoraAndAtivoTrue(
    Long idCliente,
    LocalDateTime dataHora
  );
}
