package com.tcc.uscs.model.aluno;

import com.tcc.uscs.model.aluno.dto.AtualizarAlunoDTO;
import com.tcc.uscs.model.curso.Curso;
import com.tcc.uscs.model.usuario.Usuario;
import jakarta.persistence.*;
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
  private Long id;

  @OneToOne
  @MapsId
  @JoinColumn(name = "id")
  private Usuario usuario;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "curso_id")
  private Curso curso;

  public Aluno(Usuario usuario, Curso curso) {
    this.usuario = usuario;
    this.curso = curso;
  }

  public void atualizar(AtualizarAlunoDTO dados) {
    this.usuario.atualizarInformacoes(
      dados.nome(),
      dados.email(),
      dados.telefone(),
      dados.endereco()
    );
  }

  public void excluir() {
    this.usuario.desativar();
  }
}
