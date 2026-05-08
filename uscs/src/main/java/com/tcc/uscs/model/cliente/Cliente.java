package com.tcc.uscs.model.cliente;

import com.tcc.uscs.model.cliente.dto.AtualizarClienteDTO;
import com.tcc.uscs.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "clientes")
@Entity(name = "Cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Cliente {

  @Id
  private Long id;

  @OneToOne
  @MapsId
  @JoinColumn(name = "id")
  private Usuario usuario;

  private String observacoes;

  public Cliente(Usuario usuario, String observacoes) {
    this.usuario = usuario;
    this.observacoes = observacoes;
  }

  public void atualizar(AtualizarClienteDTO dados) {
    this.usuario.atualizarInformacoes(
      dados.nome(),
      dados.email(),
      dados.telefone(),
      dados.endereco()
    );

    if (dados.observacoes() != null) {
      this.observacoes = dados.observacoes();
    }
  }

  public void excluir() {
    this.usuario.desativar();
  }
}
