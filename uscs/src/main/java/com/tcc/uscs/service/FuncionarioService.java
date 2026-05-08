package com.tcc.uscs.service;

import com.tcc.uscs.model.funcionario.Funcionario;
import com.tcc.uscs.model.funcionario.dto.*;
import com.tcc.uscs.model.usuario.TipoUsuario;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.FuncionarioRepository;
import com.tcc.uscs.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FuncionarioService {

  @Autowired
  private FuncionarioRepository repository;

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Transactional
  public DetalharFuncionarioDTO cadastrar(CadastrarFuncionarioDTO dados) {
    var usuario = new Usuario(
      dados.nome(),
      dados.cpf(),
      dados.email(),
      dados.senha(),
      dados.endereco(),
      dados.telefone(),
      TipoUsuario.FUNCIONARIO
    );
    usuarioRepository.save(usuario);

    var funcionario = new Funcionario(usuario, dados.funcao());
    repository.save(funcionario);

    return new DetalharFuncionarioDTO(funcionario);
  }

  @Transactional
  public DetalharFuncionarioDTO atualizar(AtualizarFuncionarioDTO dados) {
    var funcionario = repository.getReferenceById(dados.id());
    funcionario.atualizar(dados);
    return new DetalharFuncionarioDTO(funcionario);
  }

  @Transactional
  public void excluir(Long id) {
    var funcionario = repository.getReferenceById(id);
    funcionario.excluir();
  }
}
