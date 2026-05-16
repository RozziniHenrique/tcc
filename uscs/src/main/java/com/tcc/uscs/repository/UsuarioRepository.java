package com.tcc.uscs.repository;

import com.tcc.uscs.model.usuario.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  Page<Usuario> findAllByAtivoTrue(Pageable paginacao);

  UserDetails findByEmail(String email);
}
