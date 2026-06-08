package com.tcc.uscs.infra.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcc.uscs.infra.exception.TokenInvalidoException;
import com.tcc.uscs.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

  private final TokenService tokenService;
  private final UsuarioRepository repository;

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

        if (subject != null) {
          var usuario = repository.findByEmail(subject);
          if (usuario != null) {
            var authentication = new UsernamePasswordAuthenticationToken(
              usuario,
              null,
              usuario.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(
              authentication
            );
          } else {
            SecurityContextHolder.clearContext();
          }
        }
      } catch (TokenInvalidoException ex) {
        SecurityContextHolder.clearContext();

        estilizarRespostaErro(response, ex.getMessage());
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
      return authorizationHeader.replace("Bearer ", "");
    }

    return null;
  }

  private void estilizarRespostaErro(
    HttpServletResponse response,
    String mensagemExcecao
  ) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");

    Map<String, Object> erroJson = new HashMap<>();
    erroJson.put("status", HttpServletResponse.SC_UNAUTHORIZED);
    erroJson.put("erro", "Unauthorized");
    erroJson.put("mensagem", mensagemExcecao);
    erroJson.put("timestamp", System.currentTimeMillis());

    ObjectMapper mapper = new ObjectMapper();
    String jsonString = mapper.writeValueAsString(erroJson);

    response.getWriter().write(jsonString);
  }
}
