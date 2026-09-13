package com.tcc.uscs.repository;

import com.tcc.uscs.model.agendamento.Agendamento;
import com.tcc.uscs.model.agendamento.StatusAgendamento;
import com.tcc.uscs.model.avaliacao.dto.AvaliacaoPendenteDTO;
import com.tcc.uscs.model.relatorio.dto.AgendamentosPorCursoRelatorioDTO;
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
        AND a.status = com.tcc.uscs.model.agendamento.StatusAgendamento.CONCLUIDO
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
        c.id,
        c.nome,
        COUNT(al)
      )
      FROM Aluno al
      JOIN al.curso c
      JOIN al.usuario u
      WHERE al.ativo = true
        AND u.ativo = true
        AND c.ativo = true
      GROUP BY c.id, c.nome
      ORDER BY c.nome
    """
  )
  List<AlunosPorCursoRelatorioDTO> contarAlunosPorCurso();

  @Query(
    """
      SELECT new com.tcc.uscs.model.relatorio.dto.AgendamentosPorCursoRelatorioDTO(
        c.id,
        c.nome,
        COUNT(a),
        SUM(a.valorNoAto)
      )
      FROM Agendamento a
      JOIN a.curso c
      WHERE a.ativo = true
        AND a.status = com.tcc.uscs.model.agendamento.StatusAgendamento.CONCLUIDO
        AND c.ativo = true
        AND a.dataHora BETWEEN :inicio AND :fim
      GROUP BY c.id, c.nome
      ORDER BY c.nome
    """
  )
  List<AgendamentosPorCursoRelatorioDTO> calcularAgendamentosPorCurso(
    @Param("inicio") LocalDateTime inicio,
    @Param("fim") LocalDateTime fim
  );

  boolean existsByAlunoIdAndDataHoraAndAtivoTrueAndIdNot(
    Long idAluno,
    LocalDateTime dataHora,
    Long idAgendamento
  );

  boolean existsByClienteIdAndDataHoraAndAtivoTrueAndIdNot(
    Long idCliente,
    LocalDateTime dataHora,
    Long idAgendamento
  );

  @Query(
    """
      SELECT a
      FROM Agendamento a
      WHERE (a.cliente.id = :idUsuario OR a.aluno.id = :idUsuario)
        AND (:status IS NULL OR a.status = :status)
    """
  )
  Page<Agendamento> findAllVinculadosAoUsuario(
    @Param("idUsuario") Long idUsuario,
    @Param("status") StatusAgendamento status,
    Pageable paginacao
  );

  Page<Agendamento> findAllByStatus(
    StatusAgendamento status,
    Pageable paginacao
  );

  @Query(
    """
      SELECT new com.tcc.uscs.model.avaliacao.dto.AvaliacaoPendenteDTO(
        a.id,
        al.id,
        al.usuario.nome,
        c.id,
        c.nome,
        a.dataHora
      )
      FROM Agendamento a
      JOIN a.aluno al
      JOIN a.curso c
      WHERE a.cliente.id = :idCliente
        AND a.status = com.tcc.uscs.model.agendamento.StatusAgendamento.CONCLUIDO
        AND a.ativo = true
        AND NOT EXISTS (
          SELECT av.id
          FROM Avaliacao av
          WHERE av.agendamento = a
        )
      ORDER BY a.dataHora DESC
    """
  )
  List<AvaliacaoPendenteDTO> listarPendentesDeAvaliacao(
    @Param("idCliente") Long idCliente
  );
}
