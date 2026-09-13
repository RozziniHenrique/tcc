package com.tcc.uscs.infra.security;

import com.tcc.uscs.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AutenticacaoService implements UserDetailsService {

  private final UsuarioRepository repository;

  @Override
  public UserDetails loadUserByUsername(String username)
    throws UsernameNotFoundException {
    var usuario = repository.findByEmail(username);

    if (usuario == null) {
      throw new UsernameNotFoundException(
        "Usuário não encontrado com o email: " + username
      );
    }

    return usuario;
  }
}
