package com.tcc.uscs.model.funcionario;

import com.tcc.uscs.model.funcionario.dto.AtualizarFuncionarioDTO;
import com.tcc.uscs.model.usuario.TipoUsuario;
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

  @Column(nullable = false)
  private Boolean ativo;

  public Funcionario(Usuario usuario, Funcao funcao) {
    this.usuario = usuario;
    this.funcao = funcao;
    this.ativo = true;
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
    this.ativo = false;
    this.usuario.removerPerfil(TipoUsuario.FUNCIONARIO);
  }

  public void reativar(Funcao funcao) {
    this.ativo = true;
    this.funcao = funcao;
    this.usuario.adicionarPerfil(TipoUsuario.FUNCIONARIO);
  }
}
