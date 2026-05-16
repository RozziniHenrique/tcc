package com.tcc.uscs.infra.security;

import com.tcc.uscs.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService implements UserDetailsService {

  @Autowired
  private UsuarioRepository repository;

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
