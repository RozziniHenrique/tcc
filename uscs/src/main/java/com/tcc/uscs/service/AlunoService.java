package com.tcc.uscs.service;

import com.tcc.uscs.model.aluno.Aluno;
import com.tcc.uscs.model.aluno.dto.*;
import com.tcc.uscs.model.usuario.TipoUsuario;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AlunoService {

  @Autowired
  private AlunoRepository repository;

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private CursoRepository cursoRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Transactional
  public DetalharAlunoDTO cadastrar(CadastrarAlunoDTO dados) {
    var curso = cursoRepository.getReferenceById(dados.idCurso());
    var usuario = new Usuario(
      dados.nome(),
      dados.cpf(),
      dados.email(),
      passwordEncoder.encode(dados.senha()),
      dados.endereco(),
      dados.telefone(),
      TipoUsuario.ALUNO
    );
    usuarioRepository.save(usuario);
    var aluno = new Aluno(usuario, curso);
    repository.save(aluno);
    return new DetalharAlunoDTO(aluno);
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
