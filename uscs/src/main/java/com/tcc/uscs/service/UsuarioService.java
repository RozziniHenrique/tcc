package com.tcc.uscs.service;

import com.tcc.uscs.model.usuario.dto.DadosCadastroUsuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

  private final EntityManager entityManager;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public Long cadastrar(DadosCadastroUsuario dados) {
    String senhaCriptografada = passwordEncoder.encode(dados.senha());

    String procedureName = switch (dados.tipoUsuario()) {
      case CLIENTE -> "sp_cadastrar_usuario_cliente";
      case FUNCIONARIO -> "sp_cadastrar_usuario_funcionario";
      case ALUNO -> "sp_cadastrar_usuario_aluno";
    };

    StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
      procedureName
    );

    query.registerStoredProcedureParameter(
      "p_nome",
      String.class,
      ParameterMode.IN
    );
    query.registerStoredProcedureParameter(
      "p_cpf",
      String.class,
      ParameterMode.IN
    );
    query.registerStoredProcedureParameter(
      "p_email",
      String.class,
      ParameterMode.IN
    );
    query.registerStoredProcedureParameter(
      "p_senha",
      String.class,
      ParameterMode.IN
    );
    query.registerStoredProcedureParameter(
      "p_endereco",
      String.class,
      ParameterMode.IN
    );
    query.registerStoredProcedureParameter(
      "p_telefone",
      String.class,
      ParameterMode.IN
    );

    query.registerStoredProcedureParameter(
      "p_id",
      Long.class,
      ParameterMode.OUT
    );

    query.setParameter("p_nome", dados.nome());
    query.setParameter("p_cpf", dados.cpf());
    query.setParameter("p_email", dados.email());
    query.setParameter("p_senha", senhaCriptografada);
    query.setParameter("p_endereco", dados.enderecoCompleto());
    query.setParameter("p_telefone", dados.telefone());

    query.execute();

    return (Long) query.getOutputParameterValue("p_id");
  }
}
