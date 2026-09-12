package com.tcc.uscs.service;

import com.tcc.uscs.model.funcionario.Funcao;
import com.tcc.uscs.model.funcionario.dto.CadastrarFuncionarioDTO;
import com.tcc.uscs.repository.FuncionarioRepository;
import jakarta.validation.Validator;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
  prefix = "app.bootstrap-admin",
  name = "enabled",
  havingValue = "true"
)
public class AdministradorInicialService implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(
    AdministradorInicialService.class
  );

  private final FuncionarioRepository funcionarioRepository;
  private final FuncionarioService funcionarioService;
  private final Validator validator;

  @org.springframework.beans.factory.annotation.Value(
    "${app.bootstrap-admin.nome}"
  )
  private String nome;

  @org.springframework.beans.factory.annotation.Value(
    "${app.bootstrap-admin.cpf}"
  )
  private String cpf;

  @org.springframework.beans.factory.annotation.Value(
    "${app.bootstrap-admin.email}"
  )
  private String email;

  @org.springframework.beans.factory.annotation.Value(
    "${app.bootstrap-admin.senha}"
  )
  private String senha;

  @org.springframework.beans.factory.annotation.Value(
    "${app.bootstrap-admin.telefone}"
  )
  private String telefone;

  @org.springframework.beans.factory.annotation.Value(
    "${app.bootstrap-admin.endereco}"
  )
  private String endereco;

  @Override
  public void run(ApplicationArguments args) {
    if (
      funcionarioRepository.existsByFuncaoAndAtivoTrueAndUsuarioAtivoTrue(
        Funcao.ADMIN
      )
    ) {
      log.info("Administrador ativo já cadastrado; bootstrap ignorado.");
      return;
    }

    var dados = new CadastrarFuncionarioDTO(
      nome,
      email,
      senha,
      telefone,
      cpf,
      endereco,
      Funcao.ADMIN
    );

    var violacoes = validator.validate(dados);

    if (!violacoes.isEmpty()) {
      var camposInvalidos = violacoes
        .stream()
        .map(violacao -> violacao.getPropertyPath().toString())
        .sorted()
        .distinct()
        .collect(Collectors.joining(", "));

      throw new IllegalStateException(
        "Configuração do administrador inicial inválida. Campos: " +
          camposInvalidos
      );
    }

    funcionarioService.cadastrar(dados);

    log.info("Administrador inicial criado com sucesso.");
  }
}
