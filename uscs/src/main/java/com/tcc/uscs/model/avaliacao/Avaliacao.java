package com.tcc.uscs.model.avaliacao;

import com.tcc.uscs.model.agendamento.Agendamento;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Table(name = "avaliacoes")
@Entity(name = "Avaliacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Avaliacao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "agendamento_id", nullable = false, unique = true)
  private Agendamento agendamento;

  @Column(nullable = false)
  private Integer nota;

  @Column(length = 500)
  private String comentario;

  @Column(name = "data_avaliacao", nullable = false)
  private LocalDateTime dataAvaliacao;

  public Avaliacao(Agendamento agendamento, Integer nota, String comentario) {
    this.agendamento = agendamento;
    this.nota = nota;
    this.comentario = comentario;
    this.dataAvaliacao = LocalDateTime.now();
  }
}
