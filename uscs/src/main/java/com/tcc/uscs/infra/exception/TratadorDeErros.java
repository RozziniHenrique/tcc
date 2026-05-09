package com.tcc.uscs.infra.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadorDeErros {

  // 1. Erro 404
  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity tratarErro404() {
    return ResponseEntity.notFound().build();
  }

  // 2. Erro 400
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity tratarErro400(MethodArgumentNotValidException ex) {
    var erros = ex.getFieldErrors();
    return ResponseEntity.badRequest().body(
      erros.stream().map(DadosErroValidacao::new).toList()
    );
  }

  // 3. Erro 400
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity tratarErroRegraDeNegocio(RuntimeException ex) {
    return ResponseEntity.badRequest().body(
      new DadosErroMensagem(ex.getMessage())
    );
  }

  private record DadosErroValidacao(String campo, String mensagem) {
    public DadosErroValidacao(FieldError erro) {
      this(erro.getField(), erro.getDefaultMessage());
    }
  }

  private record DadosErroMensagem(String mensagem) {}
}
