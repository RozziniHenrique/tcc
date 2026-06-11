package com.tcc.uscs.infra.util;

import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;

public class StoredProcedureHelper {

  public static void registrarParametrosComuns(
    StoredProcedureQuery query,
    String nome,
    String cpf,
    String email,
    String senha,
    String endereco,
    String telefone
  ) {
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

    query.setParameter("p_nome", nome);
    query.setParameter("p_cpf", cpf);
    query.setParameter("p_email", email);
    query.setParameter("p_senha", senha);
    query.setParameter("p_endereco", endereco);
    query.setParameter("p_telefone", telefone);
  }
}
