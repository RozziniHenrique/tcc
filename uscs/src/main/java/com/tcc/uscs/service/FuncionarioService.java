package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.infra.helper.StoredProcedureHelper;
import com.tcc.uscs.model.funcionario.Funcionario;
import com.tcc.uscs.model.funcionario.dto.AdicionarPerfilFuncionarioDTO;
import com.tcc.uscs.model.funcionario.dto.AtualizarFuncionarioDTO;
import com.tcc.uscs.model.funcionario.dto.CadastrarFuncionarioDTO;
import com.tcc.uscs.model.funcionario.dto.DetalharFuncionarioDTO;
import com.tcc.uscs.model.funcionario.dto.ListarFuncionarioDTO;
import com.tcc.uscs.model.usuario.TipoUsuario;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.FuncionarioRepository;
import com.tcc.uscs.repository.UsuarioRepository;
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
  private final CadastroUsuarioValidator cadastroUsuarioValidator;
  private final UsuarioRepository usuarioRepository;

  public Funcionario obterEntidadePorId(Long id) {
    return repository
      .findByIdAndAtivoTrueAndUsuarioAtivoTrue(id)
      .orElseThrow(() ->
        new ValidacaoException("Funcionário não encontrado ou inativo!")
      );
  }

  @Transactional
  public DetalharFuncionarioDTO cadastrar(CadastrarFuncionarioDTO dados) {
    cadastroUsuarioValidator.validarNovoUsuario(dados.cpf(), dados.email());
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

  @Transactional
  public DetalharFuncionarioDTO adicionarPerfil(
    Long idUsuario,
    AdicionarPerfilFuncionarioDTO dados
  ) {
    var usuario = usuarioRepository
      .findById(idUsuario)
      .filter(Usuario::isEnabled)
      .orElseThrow(() ->
        new ValidacaoException("Usuário não encontrado ou inativo.")
      );

    if (usuario.getPerfis().contains(TipoUsuario.FUNCIONARIO)) {
      throw new ValidacaoException("O usuário já possui o perfil FUNCIONARIO.");
    }

    var funcionarioExistente = repository.findById(idUsuario);

    if (funcionarioExistente.isPresent()) {
      funcionarioExistente.get().reativar(dados.funcao());
      return new DetalharFuncionarioDTO(funcionarioExistente.get());
    }

    usuario.getPerfis().add(TipoUsuario.FUNCIONARIO);

    var funcionario = new Funcionario(usuario, dados.funcao());
    repository.save(funcionario);

    return new DetalharFuncionarioDTO(funcionario);
  }

  public Page<ListarFuncionarioDTO> listar(Pageable paginacao) {
    return repository
      .findAllByAtivoTrueAndUsuarioAtivoTrue(paginacao)
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
