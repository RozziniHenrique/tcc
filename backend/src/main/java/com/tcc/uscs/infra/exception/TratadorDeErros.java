package com.tcc.uscs.infra.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class TratadorDeErros {

  private static final Logger log = LoggerFactory.getLogger(
    TratadorDeErros.class
  );

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErroApiDTO> tratarErro404(EntityNotFoundException ex) {
    return resposta(
      HttpStatus.NOT_FOUND,
      "NOT_FOUND",
      "Recurso não encontrado."
    );
  }

  @ExceptionHandler(RecursoNaoEncontradoException.class)
  public ResponseEntity<ErroApiDTO> tratarRecursoNaoEncontrado(
    RecursoNaoEncontradoException ex
  ) {
    return resposta(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErroApiDTO> tratarErro400(
    MethodArgumentNotValidException ex
  ) {
    var fields = new LinkedHashMap<String, String>();
    for (FieldError erro : ex.getFieldErrors()) {
      fields.putIfAbsent(erro.getField(), erro.getDefaultMessage());
    }
    return ResponseEntity.badRequest().body(
      ErroApiDTO.validation(HttpStatus.BAD_REQUEST.value(), fields)
    );
  }

  @ExceptionHandler(ValidacaoException.class)
  public ResponseEntity<ErroApiDTO> tratarErroRegraDeNegocio(
    ValidacaoException ex
  ) {
    return resposta(HttpStatus.BAD_REQUEST, "BUSINESS_RULE", ex.getMessage());
  }

  @ExceptionHandler(
    { TokenInvalidoException.class, BadCredentialsException.class }
  )
  public ResponseEntity<ErroApiDTO> tratarErro401(Exception ex) {
    return resposta(
      HttpStatus.UNAUTHORIZED,
      "UNAUTHORIZED",
      "Credenciais ou token inválidos."
    );
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErroApiDTO> tratarErroAcessoNegado(
    AccessDeniedException ex
  ) {
    return resposta(
      HttpStatus.FORBIDDEN,
      "FORBIDDEN",
      "Acesso negado: você não possui permissão para executar esta ação."
    );
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErroApiDTO> tratarErroDuplicidade(
    DataIntegrityViolationException ex
  ) {
    return resposta(
      HttpStatus.BAD_REQUEST,
      "DATA_INTEGRITY",
      extrairMensagemDeDuplicidade(ex.getMessage())
    );
  }

  @ExceptionHandler(PersistenceException.class)
  public ResponseEntity<ErroApiDTO> tratarErroProcedure(
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
      return resposta(
        HttpStatus.BAD_REQUEST,
        "DATA_INTEGRITY",
        extrairMensagemDeDuplicidade(causaRaiz)
      );
    }
    log.error("Erro de persistência detectado", ex);
    return resposta(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "INTERNAL_ERROR",
      "Erro ao persistir os dados no banco de dados."
    );
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErroApiDTO> tratarParametroObrigatorioAusente(
    MissingServletRequestParameterException ex
  ) {
    return resposta(
      HttpStatus.BAD_REQUEST,
      "MISSING_PARAMETER",
      "O parâmetro '" + ex.getParameterName() + "' é obrigatório."
    );
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErroApiDTO> tratarParametroInvalido(
    MethodArgumentTypeMismatchException ex
  ) {
    return resposta(
      HttpStatus.BAD_REQUEST,
      "INVALID_PARAMETER",
      "O parâmetro '" + ex.getName() + "' possui um valor inválido."
    );
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErroApiDTO> tratarJsonInvalido(
    HttpMessageNotReadableException ex
  ) {
    return resposta(
      HttpStatus.BAD_REQUEST,
      "MALFORMED_JSON",
      "O corpo da requisição possui JSON inválido."
    );
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErroApiDTO> tratarRotaInexistente(
    NoResourceFoundException ex
  ) {
    return resposta(
      HttpStatus.NOT_FOUND,
      "NOT_FOUND",
      "Recurso não encontrado."
    );
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErroApiDTO> tratarMetodoNaoPermitido(
    HttpRequestMethodNotSupportedException ex
  ) {
    return resposta(
      HttpStatus.METHOD_NOT_ALLOWED,
      "METHOD_NOT_ALLOWED",
      "Método HTTP não permitido para este recurso."
    );
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErroApiDTO> tratarTipoDeConteudoInvalido(
    HttpMediaTypeNotSupportedException ex
  ) {
    return resposta(
      HttpStatus.UNSUPPORTED_MEDIA_TYPE,
      "UNSUPPORTED_MEDIA_TYPE",
      "Tipo de conteúdo não suportado."
    );
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErroApiDTO> tratarErro500(Exception ex) {
    log.error("Erro interno detectado", ex);
    return resposta(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "INTERNAL_ERROR",
      "Erro interno do servidor. Tente novamente mais tarde."
    );
  }

  private ResponseEntity<ErroApiDTO> resposta(
    HttpStatus status,
    String error,
    String message
  ) {
    return ResponseEntity.status(status).body(
      ErroApiDTO.of(status.value(), error, message)
    );
  }

  private String extrairMensagemDeDuplicidade(String mensagem) {
    if (mensagem == null) return "Erro de integridade de dados.";
    var msg = mensagem.toLowerCase();
    if (
      msg.contains("cpf")
    ) return "Já existe um usuário cadastrado com este CPF.";
    if (
      msg.contains("email")
    ) return "Já existe um usuário cadastrado com este e-mail.";
    return "Registro duplicado ou relacionamento inválido.";
  }
}
