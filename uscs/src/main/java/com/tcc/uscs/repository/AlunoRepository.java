package com.tcc.uscs.repository;

import com.tcc.uscs.model.aluno.Aluno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
  Page<Aluno> findAllByAtivoTrue(Pageable paginacao);
}
