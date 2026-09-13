package com.tcc.uscs.model.usuario;

import com.tcc.uscs.model.funcionario.Funcionario;
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

  @OneToOne(mappedBy = "usuario", fetch = FetchType.EAGER)
  private Funcionario funcionario;

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

  public boolean possuiPerfil(TipoUsuario perfil) {
    return perfis.contains(perfil);
  }

  public void adicionarPerfil(TipoUsuario perfil) {
    perfis.add(perfil);
    ativo = true;
  }

  public void removerPerfil(TipoUsuario perfil) {
    perfis.remove(perfil);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> authorities = this.perfis.stream()
      .map(perfil ->
        (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + perfil.name())
      )
      .collect(java.util.stream.Collectors.toSet());

    if (
      perfis.contains(TipoUsuario.FUNCIONARIO) &&
      funcionario != null &&
      funcionario.getFuncao() != null
    ) {
      authorities.add(
        new SimpleGrantedAuthority("ROLE_" + funcionario.getFuncao().name())
      );
    }

    return authorities;
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
