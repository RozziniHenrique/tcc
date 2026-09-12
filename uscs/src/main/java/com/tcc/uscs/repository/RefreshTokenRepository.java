package com.tcc.uscs.repository;

import com.tcc.uscs.model.usuario.RefreshToken;
import com.tcc.uscs.model.usuario.Usuario;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository
  extends JpaRepository<RefreshToken, Long>
{
  Optional<RefreshToken> findByTokenHashAndRevogadoFalse(String tokenHash);
  void deleteAllByUsuario(Usuario usuario);
  void deleteAllByDataExpiracaoBefore(LocalDateTime limite);
}
