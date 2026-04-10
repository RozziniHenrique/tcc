package com.tcc.uscs.model.cliente;

import com.tcc.uscs.model.cliente.dto.AtualizarClienteDTO;
import com.tcc.uscs.model.cliente.dto.CadastrarClienteDTO;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.*;

@Table(name = "clientes")
@Entity(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Cliente {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nome;
  private String email;
  private String senha;
  private String telefone;
  private String cpf;
  private Boolean ativo;

  public Cliente(CadastrarClienteDTO dados) {
    this.ativo = true;
    this.nome = dados.nome();
    this.senha = dados.senha();
    this.email = dados.email();
    this.telefone = dados.telefone();
    this.cpf = dados.cpf();
  }

  public void atualizarCliente(@Valid AtualizarClienteDTO dados) {
    if (dados.nome() != null) {
      this.nome = dados.nome();
    }
    if (dados.email() != null) {
      this.email = dados.email();
    }
    if (dados.senha() != null) {
      this.senha = dados.senha();
    }
    if (dados.telefone() != null) {
      this.telefone = dados.telefone();
    }
  }

  public void excluirCliente() {
    this.ativo = false;
  }

  public void reativarCliente() {
    this.ativo = true;
  }
}
