package com.tcc.uscs.model.unidade;

import com.tcc.uscs.model.unidade.dto.AtualizarUnidadeDTO;
import com.tcc.uscs.model.unidade.dto.CadastrarUnidadeDTO;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "unidades")
@Entity(name = "Unidade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Unidade {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nome;
  private String endereco;
  private String cidade;
  private String estado;
  private Boolean ativo;

  public Unidade(CadastrarUnidadeDTO dados) {
    this.nome = dados.nome();
    this.endereco = dados.endereco();
    this.cidade = dados.cidade();
    this.estado = dados.estado();
    this.ativo = true;
  }

  public void atualizar(AtualizarUnidadeDTO dados) {
    if (dados.nome() != null) this.nome = dados.nome();
    if (dados.endereco() != null) this.endereco = dados.endereco();
    if (dados.cidade() != null) this.cidade = dados.cidade();
    if (dados.estado() != null) this.estado = dados.estado();
  }

  public void excluir() {
    this.ativo = false;
  }
}
