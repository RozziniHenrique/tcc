package com.tcc.uscs.service;

import com.tcc.uscs.model.funcionario.Funcionario;
import com.tcc.uscs.model.funcionario.dto.*;
import com.tcc.uscs.model.usuario.TipoUsuario;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.FuncionarioRepository;
import com.tcc.uscs.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class FuncionarioService {

  @Autowired
  private FuncionarioRepository repository;

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Transactional
  public DetalharFuncionarioDTO cadastrar(CadastrarFuncionarioDTO dados) {
    var usuario = new Usuario(
      dados.nome(),
      dados.cpf(),
      dados.email(),
      passwordEncoder.encode(dados.senha()),
      dados.endereco(),
      dados.telefone(),
      TipoUsuario.FUNCIONARIO
    );
    usuarioRepository.save(usuario);
    var funcionario = new Funcionario(usuario, dados.funcao());
    repository.save(funcionario);
    return new DetalharFuncionarioDTO(funcionario);
  }

  public Page<ListarFuncionarioDTO> listar(Pageable paginacao) {
    return repository
      .findAllByUsuarioAtivoTrue(paginacao)
      .map(ListarFuncionarioDTO::new);
  }

  public DetalharFuncionarioDTO detalhar(Long id) {
    return new DetalharFuncionarioDTO(repository.getReferenceById(id));
  }

  @Transactional
  public DetalharFuncionarioDTO atualizar(AtualizarFuncionarioDTO dados) {
    var funcionario = repository.getReferenceById(dados.id());
    funcionario.atualizar(dados);
    return new DetalharFuncionarioDTO(funcionario);
  }

  @Transactional
  public void excluir(Long id) {
    repository.getReferenceById(id).excluir();
  }
}
