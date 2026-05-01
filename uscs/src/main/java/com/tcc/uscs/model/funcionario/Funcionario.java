package com.tcc.uscs.model.funcionario;

import com.tcc.uscs.model.funcionario.dto.AtualizarFuncionarioDTO;
import com.tcc.uscs.model.funcionario.dto.CadastrarFuncionarioDTO;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.*;

@Table(name = "funcionarios")
@Entity(name = "Funcionario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Funcionario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nome;
  private String email;
  private String senha;
  private String telefone;
  private String cpf;
  private String endereco;

  @Enumerated(EnumType.STRING)
  private Funcao funcao;

  private Boolean ativo;

  public Funcionario(CadastrarFuncionarioDTO dados) {
    this.ativo = true;
    this.nome = dados.nome();
    this.email = dados.email();
    this.senha = dados.senha();
    this.telefone = dados.telefone();
    this.cpf = dados.cpf();
    this.endereco = dados.endereco();
    this.funcao = dados.funcao();
  }

  public void atualizar(@Valid AtualizarFuncionarioDTO dados) {
    if (dados.nome() != null) this.nome = dados.nome();
    if (dados.email() != null) this.email = dados.email();
    if (dados.senha() != null) this.senha = dados.senha();
    if (dados.telefone() != null) this.telefone = dados.telefone();
    if (dados.endereco() != null) this.endereco = dados.endereco();
    if (dados.funcao() != null) this.funcao = dados.funcao();
  }

  public void excluir() {
    this.ativo = false;
  }
}
