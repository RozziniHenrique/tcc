package com.tcc.uscs.infra.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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

  // 2. Erro 400 -> Dados Inválidos
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity tratarErro400(MethodArgumentNotValidException ex) {
    var erros = ex.getFieldErrors();
    return ResponseEntity.badRequest().body(
      erros.stream().map(DadosErroValidacao::new).toList()
    );
  }

  // 3. Erro 400 -> Dados Duplicados
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity tratarErroDuplicidade(
    DataIntegrityViolationException ex
  ) {
    var mensagem = "Erro de integridade de dados.";

    if (ex.getMessage().contains("cpf")) {
      mensagem = "Já existe um usuário cadastrado com este CPF.";
    } else if (ex.getMessage().contains("email")) {
      mensagem = "Já existe um usuário cadastrado com este e-mail.";
    }

    return ResponseEntity.badRequest().body(new DadosErroMensagem(mensagem));
  }

  // 4. Erro de Regra de Negócio
  @ExceptionHandler(ValidacaoException.class)
  public ResponseEntity tratarErroRegraDeNegocio(ValidacaoException ex) {
    return ResponseEntity.badRequest().body(
      new DadosErroMensagem(ex.getMessage())
    );
  }

  // 5. Erro 500
  @ExceptionHandler(Exception.class)
  public ResponseEntity tratarErro500(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
      new DadosErroMensagem(
        "Erro interno do servidor: " + ex.getLocalizedMessage()
      )
    );
  }

  private record DadosErroValidacao(String campo, String mensagem) {
    public DadosErroValidacao(FieldError erro) {
      this(erro.getField(), erro.getDefaultMessage());
    }
  }

  private record DadosErroMensagem(String mensagem) {}
}
