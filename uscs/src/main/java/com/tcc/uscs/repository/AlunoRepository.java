package com.tcc.uscs.repository;

import com.tcc.uscs.model.aluno.Aluno;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
  Page<Aluno> findAllByUsuarioAtivoTrue(Pageable paginacao);

  List<Aluno> findAllByCursoIdAndUsuarioAtivoTrue(Long idCurso);
}
