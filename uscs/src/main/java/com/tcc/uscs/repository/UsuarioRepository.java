package com.tcc.uscs.repository;

import com.tcc.uscs.model.usuario.Usuario;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  Page<Usuario> findAllByAtivoTrue(Pageable paginacao);

  UserDetails findByEmail(String email);

  Optional<Usuario> findByEmailAndAtivoTrue(String email);

  @Procedure(value = "sp_cadastrar_usuario_cliente")
  void cadastrarCliente(
    @Param("p_nome") String nome,
    @Param("p_cpf") String cpf,
    @Param("p_email") String email,
    @Param("p_senha") String senha,
    @Param("p_endereco") String endereco,
    @Param("p_telefone") String telefone,
    @Param("p_observacoes") String observacoes
  );

  @Procedure(value = "sp_cadastrar_usuario_funcionario")
  void cadastrarFuncionario(
    @Param("p_nome") String nome,
    @Param("p_cpf") String cpf,
    @Param("p_email") String email,
    @Param("p_senha") String senha,
    @Param("p_endereco") String endereco,
    @Param("p_telefone") String telefone,
    @Param("p_funcao") String funcao
  );

  @Procedure(value = "sp_cadastrar_usuario_aluno")
  void cadastrarAluno(
    @Param("p_nome") String nome,
    @Param("p_cpf") String cpf,
    @Param("p_email") String email,
    @Param("p_senha") String senha,
    @Param("p_endereco") String endereco,
    @Param("p_telefone") String telefone,
    @Param("p_curso_id") Long cursoId
  );
}
