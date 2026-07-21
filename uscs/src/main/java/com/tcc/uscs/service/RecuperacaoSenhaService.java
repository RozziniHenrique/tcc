package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.usuario.PasswordResetToken;
import com.tcc.uscs.model.usuario.dto.RedefinirSenhaDTO;
import com.tcc.uscs.model.usuario.dto.SolicitarRecuperacaoSenhaDTO;
import com.tcc.uscs.repository.PasswordResetTokenRepository;
import com.tcc.uscs.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecuperacaoSenhaService {

  private final UsuarioRepository usuarioRepository;
  private final PasswordResetTokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public String solicitarRecuperacao(SolicitarRecuperacaoSenhaDTO dados) {
    var usuario = usuarioRepository
      .findByEmailAndAtivoTrue(dados.email())
      .orElseThrow(() ->
        new ValidacaoException("E-mail não encontrado ou usuário inativo.")
      );

    tokenRepository.deleteByUsuario(usuario);

    String token = UUID.randomUUID().toString();
    PasswordResetToken resetToken = new PasswordResetToken();
    resetToken.setToken(token);
    resetToken.setUsuario(usuario);
    resetToken.setDataExpiracao(LocalDateTime.now().plusMinutes(30));

    tokenRepository.save(resetToken);

    System.out.println(
      ">>> TOKEN DE RECUPERAÇÃO GERADO PARA " + dados.email() + ": " + token
    );
    return token;
  }

  @Transactional
  public void redefinirSenha(RedefinirSenhaDTO dados) {
    var resetToken = tokenRepository
      .findByToken(dados.token())
      .orElseThrow(() ->
        new ValidacaoException("Token de recuperação inválido ou inexistente.")
      );

    if (resetToken.isExpirado()) {
      tokenRepository.delete(resetToken);
      throw new ValidacaoException(
        "Token de recuperação expirado. Solicite uma nova redefinição."
      );
    }

    var usuario = resetToken.getUsuario();
    usuario.setSenha(passwordEncoder.encode(dados.novaSenha()));

    usuarioRepository.save(usuario);
    tokenRepository.delete(resetToken);
  }
}
