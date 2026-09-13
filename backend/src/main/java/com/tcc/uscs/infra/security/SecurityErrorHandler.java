package com.tcc.uscs.infra.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcc.uscs.infra.exception.ErroApiDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityErrorHandler
  implements AuthenticationEntryPoint, AccessDeniedHandler
{

  private final ObjectMapper objectMapper;

  @Override
  public void commence(
    HttpServletRequest request,
    HttpServletResponse response,
    AuthenticationException exception
  ) throws IOException {
    escrever(
      response,
      HttpStatus.UNAUTHORIZED,
      "UNAUTHORIZED",
      "Autenticação necessária para acessar este recurso."
    );
  }

  @Override
  public void handle(
    HttpServletRequest request,
    HttpServletResponse response,
    AccessDeniedException exception
  ) throws IOException {
    escrever(
      response,
      HttpStatus.FORBIDDEN,
      "FORBIDDEN",
      "Acesso negado: você não possui permissão para executar esta ação."
    );
  }

  private void escrever(
    HttpServletResponse response,
    HttpStatus status,
    String error,
    String message
  ) throws IOException {
    response.setStatus(status.value());
    response.setContentType("application/json;charset=UTF-8");
    response
      .getWriter()
      .write(
        objectMapper.writeValueAsString(
          ErroApiDTO.of(status.value(), error, message)
        )
      );
  }
}
