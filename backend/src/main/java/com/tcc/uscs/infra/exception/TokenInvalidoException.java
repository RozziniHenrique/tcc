package com.tcc.uscs.infra.exception;

public class TokenInvalidoException extends RuntimeException {

  public TokenInvalidoException(String mensagem) {
    super(mensagem);
  }
}
