package com.tcc.uscs.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tcc.uscs.infra.security.TokenService;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.model.usuario.dto.DadosAutenticacao;
import com.tcc.uscs.model.usuario.dto.LogoutDTO;
import com.tcc.uscs.model.usuario.dto.RefreshTokenDTO;
import com.tcc.uscs.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AutenticacaoControllerTest {

  @Mock
  private AuthenticationManager manager;

  @Mock
  private TokenService tokenService;

  @Mock
  private RefreshTokenService refreshTokenService;

  @InjectMocks
  private AutenticacaoController controller;

  @Test
  void deveAutenticarEGerarTokensDiferentes() {
    var dados = new DadosAutenticacao("usuario@email.com", "senha123");
    var usuario = mock(Usuario.class);
    var authentication = mock(Authentication.class);

    when(
      manager.authenticate(any(UsernamePasswordAuthenticationToken.class))
    ).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(usuario);
    when(tokenService.gerarAccessToken(usuario)).thenReturn("access-token");
    when(refreshTokenService.criar(usuario)).thenReturn("refresh-token");
    when(tokenService.getAccessExpirationSeconds()).thenReturn(900L);

    var resposta = controller.autenticar(dados);

    assertEquals(HttpStatus.OK, resposta.getStatusCode());
    assertNotNull(resposta.getBody());
    assertEquals("access-token", resposta.getBody().accessToken());
    assertEquals("refresh-token", resposta.getBody().refreshToken());
    assertEquals("Bearer", resposta.getBody().tokenType());
    assertEquals(900L, resposta.getBody().expiresIn());
  }

  @Test
  void deveRenovarERotacionarRefreshToken() {
    var usuario = mock(Usuario.class);
    var dados = new RefreshTokenDTO("refresh-antigo");

    when(refreshTokenService.validarEObterUsuario("refresh-antigo")).thenReturn(
      usuario
    );
    when(tokenService.gerarAccessToken(usuario)).thenReturn("novo-access");
    when(refreshTokenService.criar(usuario)).thenReturn("novo-refresh");
    when(tokenService.getAccessExpirationSeconds()).thenReturn(900L);

    var resposta = controller.renovar(dados);

    assertEquals(HttpStatus.OK, resposta.getStatusCode());
    assertNotNull(resposta.getBody());
    assertEquals("novo-access", resposta.getBody().accessToken());
    assertEquals("novo-refresh", resposta.getBody().refreshToken());

    verify(refreshTokenService).revogar("refresh-antigo");
  }

  @Test
  void deveRevogarRefreshTokenNoLogout() {
    var resposta = controller.logout(new LogoutDTO("refresh-token"));

    assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
    verify(refreshTokenService).revogar("refresh-token");
  }
}
