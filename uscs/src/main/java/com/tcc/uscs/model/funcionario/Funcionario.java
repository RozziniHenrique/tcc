package com.tcc.uscs.model.funcionario;

import com.tcc.uscs.model.funcionario.dto.AtualizarFuncionarioDTO;
import com.tcc.uscs.model.usuario.Usuario;
import jakarta.persistence.*;
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
  private Long id;

  @OneToOne
  @MapsId
  @JoinColumn(name = "id")
  private Usuario usuario;

  @Enumerated(EnumType.STRING)
  private Funcao funcao;

  public Funcionario(Usuario usuario, Funcao funcao) {
    this.usuario = usuario;
    this.funcao = funcao;
  }

  public void atualizar(AtualizarFuncionarioDTO dados) {
    this.usuario.atualizarInformacoes(
      dados.nome(),
      dados.email(),
      dados.telefone(),
      dados.endereco()
    );

    if (dados.funcao() != null) {
      this.funcao = dados.funcao();
    }
  }

  public void excluir() {
    this.usuario.desativar();
  }
}
