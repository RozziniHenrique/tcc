package com.tcc.uscs.repository;

import com.tcc.uscs.model.servico.Servico;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
  Page<Servico> findAllByAtivoTrue(Pageable paginacao);

  List<Servico> findAllByIdInAndAtivoTrue(List<Long> ids);

  Optional<Servico> findByIdAndAtivoTrue(Long id);
}
