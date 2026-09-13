package com.tcc.uscs.repository;

import com.tcc.uscs.model.usuario.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  boolean existsByCpf(String cpf);

  boolean existsByEmail(String email);

  UserDetails findByEmail(String email);

  Optional<Usuario> findByEmailAndAtivoTrue(String email);
}
