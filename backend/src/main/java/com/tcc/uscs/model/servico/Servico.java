package com.tcc.uscs.model.servico;

import com.tcc.uscs.model.servico.dto.AtualizarServicoDTO;
import com.tcc.uscs.model.servico.dto.CadastrarServicoDTO;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Table(name = "servicos")
@Entity(name = "Servico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Servico {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nome;
  private String descricao;
  private BigDecimal valor;
  private Boolean ativo;

  public Servico(CadastrarServicoDTO dados) {
    this.nome = dados.nome();
    this.descricao = dados.descricao();
    this.valor = dados.valor();
    this.ativo = true;
  }

  public void atualizar(AtualizarServicoDTO dados) {
    if (dados.nome() != null) this.nome = dados.nome();
    if (dados.descricao() != null) this.descricao = dados.descricao();
    if (dados.valor() != null) this.valor = dados.valor();
  }

  public void excluir() {
    this.ativo = false;
  }
}
