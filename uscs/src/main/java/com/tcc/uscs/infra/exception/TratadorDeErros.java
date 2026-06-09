package com.tcc.uscs.infra.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadorDeErros {

  private static final Logger log = LoggerFactory.getLogger(
    TratadorDeErros.class
  );

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

  // 3. Erro 400 -> Dados Duplicados Validação Aplicação
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity tratarErroDuplicidade(
    DataIntegrityViolationException ex
  ) {
    return ResponseEntity.badRequest().body(
      new DadosErroMensagem(extrairMensagemDeDuplicidade(ex.getMessage()))
    );
  }

  // 3.1. Erro 400 -> Dados Duplicados Validação Stored Procedure
  @ExceptionHandler(PersistenceException.class)
  public ResponseEntity tratarErroProcedureDuplicidade(
    PersistenceException ex
  ) {
    var causaRaiz =
      ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();

    if (
      causaRaiz.toLowerCase().contains("cpf") ||
      causaRaiz.toLowerCase().contains("email") ||
      causaRaiz.contains("1062")
    ) {
      return ResponseEntity.badRequest().body(
        new DadosErroMensagem(extrairMensagemDeDuplicidade(causaRaiz))
      );
    }

    log.error("Erro de persistência detectado: ", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
      new DadosErro500("Erro ao persistir os dados no banco de dados.")
    );
  }

  // 4. Erro de Regra de Negócio
  @ExceptionHandler(ValidacaoException.class)
  public ResponseEntity tratarErroRegraDeNegocio(ValidacaoException ex) {
    return ResponseEntity.badRequest().body(
      new DadosErroMensagem(ex.getMessage())
    );
  }

  // 5. Erro 500 Geral
  @ExceptionHandler(Exception.class)
  public ResponseEntity tratarErro500(Exception ex) {
    log.error("Erro interno detectado: ", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
      new DadosErro500(
        "Erro interno do servidor. Por favor, tente novamente mais tarde."
      )
    );
  }

  private String extrairMensagemDeDuplicidade(String escopoMensagem) {
    if (escopoMensagem == null) return "Erro de integridade de dados.";

    var msgMinuscula = escopoMensagem.toLowerCase();
    if (msgMinuscula.contains("cpf")) {
      return "Já existe um usuário cadastrado com este CPF.";
    } else if (msgMinuscula.contains("email")) {
      return "Já existe um usuário cadastrado com este e-mail.";
    }
    return "Erro de integridade: registro duplicado ou violação de chave estrangeira.";
  }

  private record DadosErro500(String mensagem) {}

  private record DadosErroValidacao(String campo, String mensagem) {
    public DadosErroValidacao(FieldError erro) {
      this(erro.getField(), erro.getDefaultMessage());
    }
  }

  private record DadosErroMensagem(String mensagem) {}
}
