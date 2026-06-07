package com.tcc.uscs.repository;

import com.tcc.uscs.model.servico.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
  Page<Servico> findAllByAtivoTrue(Pageable paginacao);
}
