package com.tcc.uscs.service;

import com.tcc.uscs.model.cliente.dto.*;
import com.tcc.uscs.repository.ClienteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ClienteService {

  private final ClienteRepository repository;
  private final EntityManager entityManager;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public DetalharClienteDTO cadastrar(CadastrarClienteDTO dados) {
    String senhaCriptografada = passwordEncoder.encode(dados.senha());

    StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
      "sp_cadastrar_usuario_cliente"
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
      "p_observacoes",
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
    query.setParameter("p_endereco", dados.endereco());
    query.setParameter("p_telefone", dados.telefone());
    query.setParameter("p_observacoes", dados.observacoes());

    query.execute();
    Long idGerado = (Long) query.getOutputParameterValue("p_id");

    return new DetalharClienteDTO(repository.getReferenceById(idGerado));
  }

  public Page<ListarClienteDTO> listar(Pageable paginacao) {
    return repository
      .findAllByUsuarioAtivoTrue(paginacao)
      .map(ListarClienteDTO::new);
  }

  public DetalharClienteDTO detalhar(Long id) {
    return new DetalharClienteDTO(repository.getReferenceById(id));
  }

  @Transactional
  public DetalharClienteDTO atualizar(AtualizarClienteDTO dados) {
    var cliente = repository.getReferenceById(dados.id());
    cliente.atualizar(dados);
    return new DetalharClienteDTO(cliente);
  }

  @Transactional
  public void excluir(Long id) {
    repository.getReferenceById(id).excluir();
  }
}
