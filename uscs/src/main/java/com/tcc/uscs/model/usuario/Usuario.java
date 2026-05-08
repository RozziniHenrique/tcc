package com.tcc.uscs.model.usuario;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "usuarios")
@Entity(name = "Usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nome;
  private String cpf;
  private String email;
  private String senha;
  private String enderecoCompleto;
  private String telefone;
  private TipoUsuario tipoUsuario;
  private Boolean ativo;

  public Usuario(
    String nome,
    String cpf,
    String email,
    String senha,
    String endereco,
    String telefone,
    TipoUsuario tipo
  ) {
    this.nome = nome;
    this.email = email;
    this.senha = senha;
    this.cpf = cpf;
    this.enderecoCompleto = endereco;
    this.telefone = telefone;
    this.tipoUsuario = tipo;
    this.ativo = true;
  }

  public void atualizarInformacoes(
    String nome,
    String email,
    String telefone,
    String endereco
  ) {
    if (nome != null) this.nome = nome;
    if (email != null) this.email = email;
    if (telefone != null) this.telefone = telefone;
    if (endereco != null) this.enderecoCompleto = endereco;
  }

  public void desativar() {
    this.ativo = false;
  }

  public void reativar() {
    this.ativo = true;
  }
}
