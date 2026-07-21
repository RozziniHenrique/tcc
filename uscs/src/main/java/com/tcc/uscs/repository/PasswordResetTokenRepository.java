package com.tcc.uscs.repository;

import com.tcc.uscs.model.usuario.PasswordResetToken;
import com.tcc.uscs.model.usuario.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository
  extends JpaRepository<PasswordResetToken, Long>
{
  Optional<PasswordResetToken> findByToken(String token);
  void deleteByUsuario(Usuario usuario);
}
