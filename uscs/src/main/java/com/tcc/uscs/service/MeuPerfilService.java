package com.tcc.uscs.service;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.aluno.Aluno;
import com.tcc.uscs.model.cliente.Cliente;
import com.tcc.uscs.model.me.dto.AdicionarPerfilAlunoDTO;
import com.tcc.uscs.model.me.dto.AdicionarPerfilClienteDTO;
import com.tcc.uscs.model.me.dto.AtualizarMeuPerfilDTO;
import com.tcc.uscs.model.me.dto.MeuPerfilDTO;
import com.tcc.uscs.model.usuario.TipoUsuario;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.AlunoRepository;
import com.tcc.uscs.repository.ClienteRepository;
import com.tcc.uscs.repository.CursoRepository;
import com.tcc.uscs.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeuPerfilService {

  private final UsuarioRepository usuarioRepository;
  private final ClienteRepository clienteRepository;
  private final AlunoRepository alunoRepository;
  private final CursoRepository cursoRepository;

  @Transactional(readOnly = true)
  public MeuPerfilDTO detalhar() {
    return new MeuPerfilDTO(usuarioAtual());
  }

  @Transactional
  public MeuPerfilDTO atualizar(AtualizarMeuPerfilDTO dados) {
    var usuario = usuarioAtualGerenciado();

    usuario.atualizarInformacoes(
      dados.nome(),
      null,
      dados.telefone(),
      dados.enderecoCompleto()
    );

    return new MeuPerfilDTO(usuario);
  }

  @Transactional
  public MeuPerfilDTO adicionarPerfilCliente(AdicionarPerfilClienteDTO dados) {
    var usuario = usuarioAtualGerenciado();
    validarPerfilAusente(usuario, TipoUsuario.CLIENTE);

    var clienteExistente = clienteRepository.findById(usuario.getId());

    if (clienteExistente.isPresent()) {
      clienteExistente.get().reativar(dados.observacoes());
    } else {
      usuario.getPerfis().add(TipoUsuario.CLIENTE);
      clienteRepository.save(new Cliente(usuario, dados.observacoes()));
    }

    return new MeuPerfilDTO(usuario);
  }

  @Transactional
  public MeuPerfilDTO adicionarPerfilAluno(AdicionarPerfilAlunoDTO dados) {
    var usuario = usuarioAtualGerenciado();
    validarPerfilAusente(usuario, TipoUsuario.ALUNO);

    var curso = cursoRepository
      .findByIdAndAtivoTrue(dados.idCurso())
      .orElseThrow(() ->
        new ValidacaoException("Curso não encontrado ou inativo.")
      );

    var alunoExistente = alunoRepository.findById(usuario.getId());

    if (alunoExistente.isPresent()) {
      alunoExistente.get().reativar(curso);
    } else {
      usuario.getPerfis().add(TipoUsuario.ALUNO);
      alunoRepository.save(new Aluno(usuario, curso));
    }

    return new MeuPerfilDTO(usuario);
  }

  private void validarPerfilAusente(Usuario usuario, TipoUsuario perfil) {
    if (usuario.getPerfis().contains(perfil)) {
      throw new ValidacaoException(
        "O usuário já possui o perfil " + perfil.name() + "."
      );
    }
  }

  private Usuario usuarioAtualGerenciado() {
    var principal = usuarioAtual();

    return usuarioRepository
      .findById(principal.getId())
      .filter(Usuario::isEnabled)
      .orElseThrow(() ->
        new ValidacaoException("Usuário autenticado não encontrado.")
      );
  }

  private Usuario usuarioAtual() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();

    if (
      authentication == null ||
      !authentication.isAuthenticated() ||
      !(authentication.getPrincipal() instanceof Usuario usuario) ||
      !usuario.isEnabled()
    ) {
      throw new ValidacaoException("Usuário autenticado não encontrado.");
    }

    return usuario;
  }
}
