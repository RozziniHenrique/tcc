package com.tcc.uscs.model.aluno;

import com.tcc.uscs.model.aluno.dto.AtualizarAlunoDTO;
import com.tcc.uscs.model.aluno.dto.CadastrarAlunoDTO;
import com.tcc.uscs.model.curso.Curso;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.*;

@Table(name = "alunos")
@Entity(name = "Aluno")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Aluno {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nome;
  private String email;
  private String senha;
  private String telefone;
  private String cpf;
  private String endereco;
  private Boolean ativo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "curso_id")
  private Curso curso;

  public Aluno(CadastrarAlunoDTO dados, Curso curso) {
    this.ativo = true;
    this.nome = dados.nome();
    this.email = dados.email();
    this.senha = dados.senha();
    this.telefone = dados.telefone();
    this.cpf = dados.cpf();
    this.endereco = dados.endereco();
    this.curso = curso;
  }

  public void atualizar(@Valid AtualizarAlunoDTO dados) {
    if (dados.nome() != null) this.nome = dados.nome();
    if (dados.email() != null) this.email = dados.email();
    if (dados.senha() != null) this.senha = dados.senha();
    if (dados.telefone() != null) this.telefone = dados.telefone();
    if (dados.endereco() != null) this.endereco = dados.endereco();
  }

  public void excluir() {
    this.ativo = false;
  }

  public void reativar() {
    this.ativo = true;
  }
}
