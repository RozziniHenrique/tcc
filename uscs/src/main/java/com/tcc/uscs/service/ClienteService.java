package com.tcc.uscs.service;

import com.tcc.uscs.model.cliente.Cliente;
import com.tcc.uscs.model.cliente.dto.*;
import com.tcc.uscs.model.usuario.TipoUsuario;
import com.tcc.uscs.model.usuario.Usuario;
import com.tcc.uscs.repository.ClienteRepository;
import com.tcc.uscs.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {

  @Autowired
  private ClienteRepository repository;

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Transactional
  public DetalharClienteDTO cadastrar(CadastrarClienteDTO dados) {
    var usuario = new Usuario(
      dados.nome(),
      dados.cpf(),
      dados.email(),
      passwordEncoder.encode(dados.senha()),
      dados.endereco(),
      dados.telefone(),
      TipoUsuario.CLIENTE
    );
    usuarioRepository.save(usuario);
    var cliente = new Cliente(usuario, dados.observacoes());
    repository.save(cliente);
    return new DetalharClienteDTO(cliente);
  }

  public Page<ListarClienteDTO> listar(Pageable paginacao) {
    return repository
      .findAllByUsuarioAtivoTrue(paginacao)
      .map(ListarClienteDTO::new);
  }

  public DetalharClienteDTO detalhar(Long id) {
    return new DetalharClienteDTO(repository.getReferenceById(id));
  }

  @Transactional
  public DetalharClienteDTO atualizar(AtualizarClienteDTO dados) {
    var cliente = repository.getReferenceById(dados.id());
    cliente.atualizar(dados);
    return new DetalharClienteDTO(cliente);
  }

  @Transactional
  public void excluir(Long id) {
    repository.getReferenceById(id).excluir();
  }
}
