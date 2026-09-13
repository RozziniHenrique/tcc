package com.tcc.uscs.repository;

import com.tcc.uscs.model.aluno.Aluno;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
  Page<Aluno> findAllByAtivoTrueAndUsuarioAtivoTrue(Pageable paginacao);

  Optional<Aluno> findByIdAndAtivoTrueAndUsuarioAtivoTrue(Long id);

  @Query(
    """
      SELECT al
      FROM Aluno al
      JOIN al.usuario u
      WHERE al.curso.id = :idCurso
        AND al.ativo = true
        AND u.ativo = true
        AND NOT EXISTS (
          SELECT a.id
          FROM Agendamento a
          WHERE a.aluno = al
            AND a.dataHora = :dataHora
            AND a.ativo = true
        )
    """
  )
  List<Aluno> buscarDisponiveisPorCursoEHorario(
    @Param("idCurso") Long idCurso,
    @Param("dataHora") LocalDateTime dataHora
  );
}
