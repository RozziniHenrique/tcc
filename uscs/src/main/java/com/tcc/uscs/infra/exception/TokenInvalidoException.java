package com.tcc.uscs.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class TokenInvalidoException extends RuntimeException {

  public TokenInvalidoException(String mensagem) {
    super(mensagem);
  }
}
