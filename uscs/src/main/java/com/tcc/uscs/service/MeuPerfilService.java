package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.me.dto.AtualizarMeuPerfilDTO;
import com.tcc.uscs.model.me.dto.MeuPerfilDTO;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeuPerfilService {

  private final UsuarioRepository usuarioRepository;

  @Transactional(readOnly = true)
  public MeuPerfilDTO detalhar() {
    return new MeuPerfilDTO(usuarioAtual());
  }

  @Transactional
  public MeuPerfilDTO atualizar(AtualizarMeuPerfilDTO dados) {
    var usuario = usuarioAtual();
    usuario.atualizarInformacoes(
      dados.nome(),
      null,
      dados.telefone(),
      dados.enderecoCompleto()
    );
    usuarioRepository.save(usuario);
    return new MeuPerfilDTO(usuario);
  }

  private Usuario usuarioAtual() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (
      authentication == null ||
      !authentication.isAuthenticated() ||
      !(authentication.getPrincipal() instanceof Usuario usuario) ||
      !usuario.isEnabled()
    ) {
      throw new ValidacaoException("Usuário autenticado não encontrado.");
    }
    return usuario;
  }
}
