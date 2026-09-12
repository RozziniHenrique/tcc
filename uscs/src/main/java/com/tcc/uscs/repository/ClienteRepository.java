package com.tcc.uscs.repository;

import com.tcc.uscs.model.cliente.Cliente;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
  Page<Cliente> findAllByAtivoTrueAndUsuarioAtivoTrue(Pageable paginacao);

  Optional<Cliente> findByIdAndAtivoTrueAndUsuarioAtivoTrue(Long id);
}
