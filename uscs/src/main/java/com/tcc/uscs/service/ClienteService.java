package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.infra.helper.StoredProcedureHelper;
import com.tcc.uscs.model.cliente.Cliente;
import com.tcc.uscs.model.cliente.dto.AtualizarClienteDTO;
import com.tcc.uscs.model.cliente.dto.CadastrarClienteDTO;
import com.tcc.uscs.model.cliente.dto.DetalharClienteDTO;
import com.tcc.uscs.model.cliente.dto.ListarClienteDTO;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.ClienteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ClienteService {

  private final ClienteRepository repository;
  private final EntityManager entityManager;
  private final PasswordEncoder passwordEncoder;

  public Cliente obterEntidadePorId(Long id) {
    return repository
      .findById(id)
      .orElseThrow(() ->
        new ValidacaoException("Cliente não encontrado ou inativo!")
      );
  }

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

    return detalharPorId(idGerado);
  }

  public Page<ListarClienteDTO> listar(Pageable paginacao) {
    return repository
      .findAllByUsuarioAtivoTrue(paginacao)
      .map(ListarClienteDTO::new);
  }

  public DetalharClienteDTO detalhar(Long id) {
    validarPosseDoRecurso(id);
    return detalharPorId(id);
  }

  @Transactional
  public DetalharClienteDTO atualizar(Long id, AtualizarClienteDTO dados) {
    validarPosseDoRecurso(id);
    var cliente = obterEntidadePorId(id);
    cliente.atualizar(dados);
    return new DetalharClienteDTO(cliente);
  }

  @Transactional
  public void excluir(Long id) {
    var cliente = obterEntidadePorId(id);
    cliente.excluir();
  }

  private DetalharClienteDTO detalharPorId(Long id) {
    var cliente = obterEntidadePorId(id);
    return new DetalharClienteDTO(cliente);
  }

  private void validarPosseDoRecurso(Long clienteId) {
    var usuarioLogado = (Usuario) SecurityContextHolder.getContext()
      .getAuthentication()
      .getPrincipal();
    boolean isFuncionario = usuarioLogado
      .getAuthorities()
      .stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_FUNCIONARIO"));

    if (!isFuncionario && !usuarioLogado.getId().equals(clienteId)) {
      throw new AccessDeniedException(
        "Você não tem permissão para acessar ou alterar dados deste cliente."
      );
    }
  }
}
