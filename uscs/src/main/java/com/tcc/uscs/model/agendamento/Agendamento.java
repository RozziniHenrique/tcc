package com.tcc.uscs.model.agendamento;

import com.tcc.uscs.model.aluno.Aluno;
import com.tcc.uscs.model.cliente.Cliente;
import com.tcc.uscs.model.curso.Curso;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Table(name = "agendamentos")
@Entity(name = "Agendamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Agendamento {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cliente_id")
  private Cliente cliente;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "aluno_id")
  private Aluno aluno;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "curso_id")
  private Curso curso;

  @Column(name = "data_hora")
  private LocalDateTime dataHora;

  @Column(name = "valor_no_ato")
  private BigDecimal valorNoAto; //Salva o preço atual no momento do agendamento

  @Column(name = "ativo")
  private Boolean ativo;

  @Column(name = "justificativa_cancelamento")
  private String justificativaCancelamento;

  public Agendamento(
    Cliente cliente,
    Aluno aluno,
    Curso curso,
    LocalDateTime dataHora
  ) {
    this.cliente = cliente;
    this.aluno = aluno;
    this.curso = curso;
    this.dataHora = dataHora;
    this.ativo = true;
  }

  public void cancelar(String justificativa) {
    this.ativo = false;
    this.justificativaCancelamento = justificativa;
  }
}
