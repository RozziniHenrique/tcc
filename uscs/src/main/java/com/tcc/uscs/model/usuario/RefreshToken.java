package com.tcc.uscs.model.usuario;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Table(name = "refresh_tokens")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @Column(name = "data_expiracao", nullable = false)
  private LocalDateTime dataExpiracao;

  @Column(nullable = false)
  private boolean revogado;

  @Column(name = "criado_em", nullable = false)
  private LocalDateTime criadoEm;

  public boolean isExpirado() {
    return LocalDateTime.now().isAfter(dataExpiracao);
  }
}
