package com.tcc.uscs.repository;

import com.tcc.uscs.model.curso.Curso;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {
  Page<Curso> findAllByAtivoTrue(Pageable paginacao);

  Optional<Curso> findByIdAndAtivoTrue(Long id);
}
