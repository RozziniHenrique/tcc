package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.infra.helper.StoredProcedureHelper;
import com.tcc.uscs.model.aluno.Aluno;
import com.tcc.uscs.model.aluno.dto.AtualizarAlunoDTO;
import com.tcc.uscs.model.aluno.dto.CadastrarAlunoDTO;
import com.tcc.uscs.model.aluno.dto.DetalharAlunoDTO;
import com.tcc.uscs.model.aluno.dto.ListarAlunoDTO;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.AlunoRepository;
import com.tcc.uscs.repository.CursoRepository;
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
  private final CadastroUsuarioValidator cadastroUsuarioValidator;
  private final CursoRepository cursoRepository;

  public Long buscarAlunoAleatorio(Long idCurso) {
    var disponiveis =
      repository.findAllByCursoIdAndAtivoTrueAndUsuarioAtivoTrue(idCurso);

    if (disponiveis.isEmpty()) {
      throw new ValidacaoException("Nenhum aluno disponível para este curso.");
    }
    int indiceAleatorio = ThreadLocalRandom.current().nextInt(
      disponiveis.size()
    );
    return disponiveis.get(indiceAleatorio).getId();
  }

  public Aluno obterEntidadePorId(Long id) {
    return repository
      .findByIdAndAtivoTrueAndUsuarioAtivoTrue(id)
      .orElseThrow(() ->
        new ValidacaoException("Aluno não encontrado ou inativo!")
      );
  }

  @Transactional
  public DetalharAlunoDTO cadastrar(CadastrarAlunoDTO dados) {
    cadastroUsuarioValidator.validarNovoUsuario(dados.cpf(), dados.email());
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

    return detalharPorId(idGerado);
  }

  public Page<ListarAlunoDTO> listar(Pageable paginacao) {
    return repository
      .findAllByAtivoTrueAndUsuarioAtivoTrue(paginacao)
      .map(ListarAlunoDTO::new);
  }

  public DetalharAlunoDTO detalhar(Long id) {
    validarPosseDoRecurso(id);
    return detalharPorId(id);
  }

  @Transactional
  public DetalharAlunoDTO atualizar(Long id, AtualizarAlunoDTO dados) {
    validarPosseDoRecurso(id);
    var aluno = obterEntidadePorId(id);
    aluno.atualizar(dados);
    if (dados.idCurso() != null) {
      var curso = cursoRepository
        .findByIdAndAtivoTrue(dados.idCurso())
        .orElseThrow(() ->
          new ValidacaoException("Curso não encontrado ou inativo.")
        );

      aluno.setCurso(curso);
    }
    return new DetalharAlunoDTO(aluno);
  }

  @Transactional
  public void excluir(Long id) {
    var aluno = obterEntidadePorId(id);
    aluno.excluir();
  }

  private DetalharAlunoDTO detalharPorId(Long id) {
    var aluno = obterEntidadePorId(id);
    return new DetalharAlunoDTO(aluno);
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

  @Transactional(readOnly = true)
  public Aluno obterEntidadePorIdECurso(Long idAluno, Long idCurso) {
    var aluno = obterEntidadePorId(idAluno);

    if (aluno.getCurso() == null || !aluno.getCurso().getId().equals(idCurso)) {
      throw new ValidacaoException(
        "O aluno informado não pertence ao curso selecionado."
      );
    }

    return aluno;
  }
}
