package com.tcc.uscs.model.usuario;

import static org.junit.jupiter.api.Assertions.*;

import com.tcc.uscs.model.cliente.Cliente;
import com.tcc.uscs.model.funcionario.Funcao;
import com.tcc.uscs.model.funcionario.Funcionario;
import org.junit.jupiter.api.Test;

class UsuarioPerfisTest {

  @Test
  void removerUmPerfilDeveManterContaComOutrosPerfis() {
    var usuario = new Usuario();
    usuario.adicionarPerfil(TipoUsuario.CLIENTE);
    usuario.adicionarPerfil(TipoUsuario.ALUNO);

    var cliente = new Cliente(usuario, null);
    cliente.excluir();

    assertFalse(cliente.getAtivo());
    assertFalse(usuario.possuiPerfil(TipoUsuario.CLIENTE));
    assertTrue(usuario.possuiPerfil(TipoUsuario.ALUNO));
    assertTrue(usuario.isEnabled());
  }

  @Test
  void removerUltimoPerfilDeveDesativarConta() {
    var usuario = new Usuario();
    usuario.adicionarPerfil(TipoUsuario.CLIENTE);

    var cliente = new Cliente(usuario, null);
    cliente.excluir();

    assertFalse(usuario.isEnabled());
    assertTrue(usuario.getPerfis().isEmpty());
  }

  @Test
  void reativarPerfilDeveReativarConta() {
    var usuario = new Usuario();
    usuario.adicionarPerfil(TipoUsuario.CLIENTE);

    var cliente = new Cliente(usuario, "Antiga");
    cliente.excluir();
    cliente.reativar("Nova observação");

    assertTrue(cliente.getAtivo());
    assertTrue(usuario.isEnabled());
    assertTrue(usuario.possuiPerfil(TipoUsuario.CLIENTE));
    assertEquals("Nova observação", cliente.getObservacoes());
  }

  @Test
  void removerFuncionarioDeveRemoverSuaFuncaoDasAutoridades() {
    var usuario = new Usuario();
    usuario.adicionarPerfil(TipoUsuario.FUNCIONARIO);

    var funcionario = new Funcionario(usuario, Funcao.ADMIN);
    usuario.setFuncionario(funcionario);

    assertTrue(
      usuario
        .getAuthorities()
        .stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
    );

    funcionario.excluir();

    assertFalse(funcionario.getAtivo());
    assertFalse(usuario.possuiPerfil(TipoUsuario.FUNCIONARIO));
    assertFalse(
      usuario
        .getAuthorities()
        .stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
    );
  }
}
