package com.tcc.uscs.repository;

import com.tcc.uscs.model.funcionario.Funcao;
import com.tcc.uscs.model.funcionario.Funcionario;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuncionarioRepository
  extends JpaRepository<Funcionario, Long>
{
  Page<Funcionario> findAllByAtivoTrueAndUsuarioAtivoTrue(Pageable paginacao);

  Optional<Funcionario> findByIdAndAtivoTrueAndUsuarioAtivoTrue(Long id);

  boolean existsByFuncaoAndAtivoTrueAndUsuarioAtivoTrue(Funcao funcao);
}
