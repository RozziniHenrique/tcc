package com.tcc.uscs.service;

import com.tcc.uscs.model.aluno.Aluno;
import com.tcc.uscs.model.aluno.dto.*;
import com.tcc.uscs.model.usuario.TipoUsuario;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.AlunoRepository;
import com.tcc.uscs.repository.CursoRepository;
import com.tcc.uscs.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlunoService {

  @Autowired
  private AlunoRepository repository;

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private CursoRepository cursoRepository;

  @Transactional
  public DetalharAlunoDTO cadastrar(CadastrarAlunoDTO dados) {
    if (!cursoRepository.existsById(dados.idCurso())) {
      throw new RuntimeException("Curso informado não existe!");
    }
    var curso = cursoRepository.getReferenceById(dados.idCurso());

    var usuario = new Usuario(
      dados.nome(),
      dados.cpf(),
      dados.email(),
      dados.senha(),
      dados.endereco(),
      dados.telefone(),
      TipoUsuario.ALUNO
    );
    usuarioRepository.save(usuario);

    var aluno = new Aluno(usuario, curso);
    repository.save(aluno);

    return new DetalharAlunoDTO(aluno);
  }

  @Transactional
  public DetalharAlunoDTO atualizar(AtualizarAlunoDTO dados) {
    var aluno = repository.getReferenceById(dados.id());
    aluno.atualizar(dados);
    return new DetalharAlunoDTO(aluno);
  }

  @Transactional
  public void excluir(Long id) {
    var aluno = repository.getReferenceById(id);
    aluno.excluir();
  }
}
