package com.tcc.uscs.service;

import com.tcc.uscs.infra.util.StoredProcedureHelper;
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

    StoredProcedureHelper.registrarParametrosComuns(
      query,
      dados.nome(),
      dados.cpf(),
      dados.email(),
      senhaCriptografada,
      dados.endereco(),
      dados.telefone()
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
  public DetalharClienteDTO atualizar(Long id, AtualizarClienteDTO dados) {
    var cliente = repository.getReferenceById(id);
    cliente.atualizar(dados);
    return new DetalharClienteDTO(cliente);
  }

  @Transactional
  public void excluir(Long id) {
    repository.getReferenceById(id).excluir();
  }
}
