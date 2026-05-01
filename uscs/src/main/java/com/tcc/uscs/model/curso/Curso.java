package com.tcc.uscs.model.curso;

import com.tcc.uscs.model.curso.dto.AtualizarCursoDTO;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import lombok.*;

@Table(name = "cursos")
@Entity(name = "Curso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Curso {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nome;
  private String descricao;
  private String periodo;
  private String duracao;
  private String anoVigente;
  private BigDecimal valor;
  private Boolean ativo;

  public void atualizar(@Valid AtualizarCursoDTO dados) {
    if (dados.nome() != null) this.nome = dados.nome();
    if (dados.descricao() != null) this.descricao = dados.descricao();
    if (dados.periodo() != null) this.periodo = dados.periodo();
    if (dados.duracao() != null) this.duracao = dados.duracao();
    if (dados.anoVigente() != null) this.anoVigente = dados.anoVigente();
    if (dados.valor() != null) this.valor = dados.valor();
  }
}
