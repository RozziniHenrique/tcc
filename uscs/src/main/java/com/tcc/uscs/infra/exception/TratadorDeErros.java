package com.tcc.uscs.infra.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadorDeErros {

  private static final Logger log = LoggerFactory.getLogger(
    TratadorDeErros.class
  );

  // 1. Erro 404 - Entidade não encontrada
  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<Void> tratarErro404() {
    return ResponseEntity.notFound().build();
  }

  // 2. Erro 400 - Falha de validação de DTO (@Valid / Bean Validation)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<List<DadosErroValidacao>> tratarErro400(
    MethodArgumentNotValidException ex
  ) {
    var erros = ex.getFieldErrors();
    return ResponseEntity.badRequest().body(
      erros.stream().map(DadosErroValidacao::new).toList()
    );
  }

  // 3. Erro 400 - Regra de Negócio
  @ExceptionHandler(ValidacaoException.class)
  public ResponseEntity<DadosErroMensagem> tratarErroRegraDeNegocio(
    ValidacaoException ex
  ) {
    return ResponseEntity.badRequest().body(
      new DadosErroMensagem(ex.getMessage())
    );
  }

  // 4. Erro 401 - Token Inválido / Expirado
  @ExceptionHandler(TokenInvalidoException.class)
  public ResponseEntity<DadosErroMensagem> tratarErroTokenInvalido(
    TokenInvalidoException ex
  ) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
      new DadosErroMensagem(ex.getMessage())
    );
  }

  // 5. Erro 403 - Acesso Negado (Ownership / Role)
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<DadosErroMensagem> tratarErroAcessoNegado(
    AccessDeniedException ex
  ) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
      new DadosErroMensagem(
        "Acesso negado: você não possui permissão para executar esta ação."
      )
    );
  }

  // 6. Erro 400 - Duplicidade / Violência de Constraints no Banco
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<DadosErroMensagem> tratarErroDuplicidade(
    DataIntegrityViolationException ex
  ) {
    return ResponseEntity.badRequest().body(
      new DadosErroMensagem(extrairMensagemDeDuplicidade(ex.getMessage()))
    );
  }

  // 6.1. Erro 400 / 500 - Exceções de Persistência e Stored Procedures
  @ExceptionHandler(PersistenceException.class)
  public ResponseEntity<Object> tratarErroProcedureDuplicidade(
    PersistenceException ex
  ) {
    var causaRaiz =
      ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();

    if (
      causaRaiz != null &&
      (causaRaiz.toLowerCase().contains("cpf") ||
        causaRaiz.toLowerCase().contains("email") ||
        causaRaiz.contains("1062"))
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

  // 7. Erro 500 - Exceções Não Tratadas
  @ExceptionHandler(Exception.class)
  public ResponseEntity<DadosErro500> tratarErro500(Exception ex) {
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

  // DTOs Internos para Padronização de JSONs de Erro
  private record DadosErro500(String mensagem) {}

  private record DadosErroValidacao(String campo, String mensagem) {
    public DadosErroValidacao(FieldError erro) {
      this(erro.getField(), erro.getDefaultMessage());
    }
  }

  private record DadosErroMensagem(String mensagem) {}
}
