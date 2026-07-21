package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.infra.util.StoredProcedureHelper;
import com.tcc.uscs.model.aluno.Aluno;
import com.tcc.uscs.model.aluno.dto.AtualizarAlunoDTO;
import com.tcc.uscs.model.aluno.dto.CadastrarAlunoDTO;
import com.tcc.uscs.model.aluno.dto.DetalharAlunoDTO;
import com.tcc.uscs.model.aluno.dto.ListarAlunoDTO;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.AlunoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import java.util.concurrent.ThreadLocalRandom;
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
public class AlunoService {

  private final AlunoRepository repository;
  private final EntityManager entityManager;
  private final PasswordEncoder passwordEncoder;

  public Long buscarAlunoAleatorio(Long idCurso) {
    var disponiveis = repository.findAllByCursoIdAndUsuarioAtivoTrue(idCurso);

    if (disponiveis.isEmpty()) {
      throw new ValidacaoException("Nenhum aluno disponível para este curso.");
    }
    int indiceAleatorio = ThreadLocalRandom.current().nextInt(
      disponiveis.size()
    );
    return disponiveis.get(indiceAleatorio).getId();
  }

  public Aluno obterReferencia(Long id) {
    if (!repository.existsById(id)) {
      throw new ValidacaoException("Aluno não encontrado ou inativo!");
    }
    return repository.getReferenceById(id);
  }

  @Transactional
  public DetalharAlunoDTO cadastrar(CadastrarAlunoDTO dados) {
    String senhaCriptografada = passwordEncoder.encode(dados.senha());

    StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
      "sp_cadastrar_usuario_aluno"
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
      "p_curso_id",
      Long.class,
      ParameterMode.IN
    );
    query.registerStoredProcedureParameter(
      "p_id",
      Long.class,
      ParameterMode.OUT
    );

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
    validarPosseDoRecurso(id);
    return new DetalharAlunoDTO(repository.getReferenceById(id));
  }

  @Transactional
  public DetalharAlunoDTO atualizar(Long id, AtualizarAlunoDTO dados) {
    validarPosseDoRecurso(id);
    var aluno = repository.getReferenceById(id);
    aluno.atualizar(dados);
    return new DetalharAlunoDTO(aluno);
  }

  @Transactional
  public void excluir(Long id) {
    repository.getReferenceById(id).excluir();
  }

  private void validarPosseDoRecurso(Long alunoId) {
    var usuarioLogado = (Usuario) SecurityContextHolder.getContext()
      .getAuthentication()
      .getPrincipal();
    boolean isFuncionario = usuarioLogado
      .getAuthorities()
      .stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_FUNCIONARIO"));

    if (!isFuncionario && !usuarioLogado.getId().equals(alunoId)) {
      throw new AccessDeniedException(
        "Você não tem permissão para acessar ou alterar dados deste aluno."
      );
    }
  }
}
