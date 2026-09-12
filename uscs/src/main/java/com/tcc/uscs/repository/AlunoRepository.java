package com.tcc.uscs.repository;

import com.tcc.uscs.model.aluno.Aluno;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
  Page<Aluno> findAllByAtivoTrueAndUsuarioAtivoTrue(Pageable paginacao);

  Optional<Aluno> findByIdAndAtivoTrueAndUsuarioAtivoTrue(Long id);

  List<Aluno> findAllByCursoIdAndAtivoTrueAndUsuarioAtivoTrue(Long idCurso);
}
