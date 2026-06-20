package com.tcc.uscs.model.usuario;

import com.tcc.uscs.model.usuario.dto.DadosCadastroUsuario;
import jakarta.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Table(name = "usuarios")
@Entity(name = "Usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Usuario implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nome;

  @Column(unique = true, nullable = false)
  private String cpf;

  @Column(unique = true, nullable = false)
  private String email;

  private String senha;
  private String enderecoCompleto;
  private String telefone;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
    name = "perfis_usuario",
    joinColumns = @JoinColumn(name = "usuario_id")
  )
  @Column(name = "perfil")
  @Enumerated(EnumType.STRING)
  private Set<TipoUsuario> perfis = new HashSet<>();

  private Boolean ativo;

  public Usuario(DadosCadastroUsuario dados, String senhaCriptografada) {
    this.nome = dados.nome();
    this.cpf = dados.cpf();
    this.email = dados.email();
    this.senha = senhaCriptografada;
    this.enderecoCompleto = dados.enderecoCompleto();
    this.telefone = dados.telefone();
    this.perfis.add(dados.tipoUsuario());
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

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return this.perfis.stream()
      .map(perfil -> new SimpleGrantedAuthority("ROLE_" + perfil.name()))
      .toList();
  }

  @Override
  public String getPassword() {
    return senha;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return ativo;
  }
}
