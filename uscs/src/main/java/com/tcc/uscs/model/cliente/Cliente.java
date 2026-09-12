package com.tcc.uscs.model.cliente;

import com.tcc.uscs.model.cliente.dto.AtualizarClienteDTO;
import com.tcc.uscs.model.usuario.TipoUsuario;
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

  @Column(nullable = false)
  private Boolean ativo;

  public Cliente(Usuario usuario, String observacoes) {
    this.usuario = usuario;
    this.observacoes = observacoes;
    this.ativo = true;
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
    this.ativo = false;
    this.usuario.removerPerfil(TipoUsuario.CLIENTE);
  }

  public void reativar(String observacoes) {
    this.ativo = true;
    this.usuario.adicionarPerfil(TipoUsuario.CLIENTE);

    if (observacoes != null) {
      this.observacoes = observacoes;
    }
  }
}
