package com.tcc.uscs.model.usuario;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Table(name = "password_reset_tokens")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String token;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @Column(nullable = false)
  private LocalDateTime dataExpiracao;

  public boolean isExpirado() {
    return LocalDateTime.now().isAfter(dataExpiracao);
  }
}
