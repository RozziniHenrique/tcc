package com.tcc.uscs.infra.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcc.uscs.infra.exception.ErroApiDTO;
import com.tcc.uscs.infra.exception.TokenInvalidoException;
import com.tcc.uscs.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

  private final TokenService tokenService;
  private final UsuarioRepository repository;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    var tokenJWT = recuperarToken(request);

    if (tokenJWT != null) {
      try {
        var subject = tokenService.getSubject(tokenJWT);
        var usuarioOpt = repository.findByEmailAndAtivoTrue(subject);
        if (usuarioOpt.isPresent()) {
          var usuario = usuarioOpt.get();
          var authentication = new UsernamePasswordAuthenticationToken(
            usuario,
            null,
            usuario.getAuthorities()
          );
          SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
          SecurityContextHolder.clearContext();
        }
      } catch (TokenInvalidoException ex) {
        SecurityContextHolder.clearContext();
        escreverNaoAutorizado(response);
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  private String recuperarToken(HttpServletRequest request) {
    var authorizationHeader = request.getHeader("Authorization");
    if (
      authorizationHeader != null && authorizationHeader.startsWith("Bearer ")
    ) {
      return authorizationHeader.substring(7).trim();
    }
    return null;
  }

  private void escreverNaoAutorizado(HttpServletResponse response)
    throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");
    var erro = ErroApiDTO.of(
      HttpStatus.UNAUTHORIZED.value(),
      "UNAUTHORIZED",
      "Credenciais ou token inválidos."
    );
    response.getWriter().write(objectMapper.writeValueAsString(erro));
  }
}
