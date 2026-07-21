package com.tcc.uscs.repository;

import com.tcc.uscs.model.agendamento.Agendamento;
import com.tcc.uscs.model.relatorio.dto.AlunosPorCursoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.FaturamentoRelatorioDTO;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgendamentoRepository
  extends JpaRepository<Agendamento, Long>
{
  Page<Agendamento> findAllByAtivoTrue(Pageable paginacao);

  Page<Agendamento> findAllByClienteIdAndAtivoTrue(
    Long idCliente,
    Pageable paginacao
  );

  Page<Agendamento> findAllByAlunoIdAndAtivoTrue(
    Long idAluno,
    Pageable paginacao
  );

  boolean existsByAlunoIdAndDataHoraAndAtivoTrue(
    Long idAluno,
    LocalDateTime dataHora
  );

  boolean existsByClienteIdAndDataHoraAndAtivoTrue(
    Long idCliente,
    LocalDateTime dataHora
  );

  @Query(
    """
        SELECT new com.tcc.uscs.model.relatorio.dto.FaturamentoRelatorioDTO(
            COUNT(a),
            SUM(a.valorNoAto)
        )
        FROM Agendamento a
        WHERE a.ativo = true
        AND a.dataHora BETWEEN :inicio AND :fim
    """
  )
  FaturamentoRelatorioDTO calcularFaturamentoPorPeriodo(
    @Param("inicio") LocalDateTime inicio,
    @Param("fim") LocalDateTime fim
  );

  @Query(
    """
        SELECT new com.tcc.uscs.model.relatorio.dto.AlunosPorCursoRelatorioDTO(
            c.nome,
            COUNT(al)
        )
        FROM Aluno al
        JOIN al.curso c
        GROUP BY c.nome
    """
  )
  List<AlunosPorCursoRelatorioDTO> contarAlunosPorCurso();
}
