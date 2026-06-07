package com.tcc.uscs.service;

import com.tcc.uscs.model.aluno.dto.*;
import com.tcc.uscs.repository.AlunoRepository;
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
public class AlunoService {

  private final AlunoRepository repository;
  private final EntityManager entityManager;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public DetalharAlunoDTO cadastrar(CadastrarAlunoDTO dados) {
    String senhaCriptografada = passwordEncoder.encode(dados.senha());

    StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
      "sp_cadastrar_usuario_aluno"
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
      "p_curso_id",
      Long.class,
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
    query.setParameter("p_curso_id", dados.idCurso());

    query.execute();
    Long idGerado = (Long) query.getOutputParameterValue("p_id");

    return new DetalharAlunoDTO(repository.getReferenceById(idGerado));
  }

  public Page<ListarAlunoDTO> listar(Pageable paginacao) {
    return repository
      .findAllByUsuarioAtivoTrue(paginacao)
      .map(ListarAlunoDTO::new);
  }

  public DetalharAlunoDTO detalhar(Long id) {
    return new DetalharAlunoDTO(repository.getReferenceById(id));
  }

  @Transactional
  public DetalharAlunoDTO atualizar(AtualizarAlunoDTO dados) {
    var aluno = repository.getReferenceById(dados.id());
    aluno.atualizar(dados);
    return new DetalharAlunoDTO(aluno);
  }

  @Transactional
  public void excluir(Long id) {
    repository.getReferenceById(id).excluir();
  }
}
