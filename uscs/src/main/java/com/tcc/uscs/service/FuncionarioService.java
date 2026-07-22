package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.infra.helper.StoredProcedureHelper;
import com.tcc.uscs.model.funcionario.Funcionario;
import com.tcc.uscs.model.funcionario.dto.AtualizarFuncionarioDTO;
import com.tcc.uscs.model.funcionario.dto.CadastrarFuncionarioDTO;
import com.tcc.uscs.model.funcionario.dto.DetalharFuncionarioDTO;
import com.tcc.uscs.model.funcionario.dto.ListarFuncionarioDTO;
import com.tcc.uscs.repository.FuncionarioRepository;
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
public class FuncionarioService {

  private final FuncionarioRepository repository;
  private final EntityManager entityManager;
  private final PasswordEncoder passwordEncoder;

  public Funcionario obterEntidadePorId(Long id) {
    return repository
      .findById(id)
      .orElseThrow(() ->
        new ValidacaoException("Funcionário não encontrado ou inativo!")
      );
  }

  @Transactional
  public DetalharFuncionarioDTO cadastrar(CadastrarFuncionarioDTO dados) {
    String senhaCriptografada = passwordEncoder.encode(dados.senha());

    StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
      "sp_cadastrar_usuario_funcionario"
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
      "p_funcao",
      String.class,
      ParameterMode.IN
    );
    query.registerStoredProcedureParameter(
      "p_id",
      Long.class,
      ParameterMode.OUT
    );

    query.setParameter(
      "p_funcao",
      dados.funcao() != null ? dados.funcao().toString() : null
    );

    query.execute();
    Long idGerado = (Long) query.getOutputParameterValue("p_id");

    return detalharPorId(idGerado);
  }

  public Page<ListarFuncionarioDTO> listar(Pageable paginacao) {
    return repository
      .findAllByUsuarioAtivoTrue(paginacao)
      .map(ListarFuncionarioDTO::new);
  }

  public DetalharFuncionarioDTO detalhar(Long id) {
    return detalharPorId(id);
  }

  @Transactional
  public DetalharFuncionarioDTO atualizar(
    Long id,
    AtualizarFuncionarioDTO dados
  ) {
    var funcionario = obterEntidadePorId(id);
    funcionario.atualizar(dados);
    return new DetalharFuncionarioDTO(funcionario);
  }

  @Transactional
  public void excluir(Long id) {
    var funcionario = obterEntidadePorId(id);
    funcionario.excluir();
  }

  private DetalharFuncionarioDTO detalharPorId(Long id) {
    var funcionario = obterEntidadePorId(id);
    return new DetalharFuncionarioDTO(funcionario);
  }
}
