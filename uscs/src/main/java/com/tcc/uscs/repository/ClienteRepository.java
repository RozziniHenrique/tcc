package com.tcc.uscs.repository;

import com.tcc.uscs.model.cliente.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
  Page<Cliente> findAllByAtivoTrue(Pageable paginacao);
}
