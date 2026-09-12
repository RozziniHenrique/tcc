package com.tcc.uscs.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.tcc.uscs.model.agendamento.Agendamento;
import com.tcc.uscs.model.agendamento.StatusAgendamento;
import com.tcc.uscs.model.aluno.Aluno;
import com.tcc.uscs.model.cliente.Cliente;
import com.tcc.uscs.model.curso.Curso;
import com.tcc.uscs.model.unidade.Unidade;
import com.tcc.uscs.model.usuario.Usuario;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest(
  properties = {
    "spring.datasource.url=jdbc:h2:mem:relatorio_repository_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
    "spring.jpa.hibernate.ddl-auto=create-only",
    "spring.jpa.show-sql=false",
  }
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class RelatorioRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private AgendamentoRepository agendamentoRepository;

  private Curso curso;
  private Cliente cliente;
  private Aluno aluno;
  private Unidade unidade;

  @BeforeEach
  void prepararDados() {
    curso = new Curso();
    curso.setNome("Estética");
    curso.setDescricao("Curso de estética");
    curso.setPeriodo("Noturno");
    curso.setDuracao("12 meses");
    curso.setAnoVigente("2026");
    curso.setValor(new BigDecimal("1000.00"));
    curso.setAtivo(true);
    entityManager.persist(curso);

    var usuarioCliente = criarUsuario("11111111111", "cliente@teste.com", true);
    cliente = new Cliente(usuarioCliente, null);
    entityManager.persist(cliente);

    var usuarioAluno = criarUsuario("22222222222", "aluno@teste.com", true);
    aluno = new Aluno(usuarioAluno, curso);
    entityManager.persist(aluno);

    var usuarioAlunoInativo = criarUsuario(
      "33333333333",
      "inativo@teste.com",
      false
    );
    entityManager.persist(new Aluno(usuarioAlunoInativo, curso));

    unidade = new Unidade();
    unidade.setNome("Unidade Centro");
    unidade.setEndereco("Rua Teste, 100");
    unidade.setCidade("São Paulo");
    unidade.setEstado("SP");
    unidade.setAtivo(true);
    entityManager.persist(unidade);

    criarAgendamento(
      LocalDateTime.of(2026, 1, 10, 10, 0),
      new BigDecimal("100.00"),
      StatusAgendamento.CONCLUIDO
    );

    criarAgendamento(
      LocalDateTime.of(2026, 1, 11, 10, 0),
      new BigDecimal("200.00"),
      StatusAgendamento.AGENDADO
    );

    criarAgendamento(
      LocalDateTime.of(2026, 1, 12, 10, 0),
      new BigDecimal("300.00"),
      StatusAgendamento.CANCELADO
    );

    criarAgendamento(
      LocalDateTime.of(2025, 12, 10, 10, 0),
      new BigDecimal("400.00"),
      StatusAgendamento.CONCLUIDO
    );

    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void deveriaCalcularSomenteAgendamentosConcluidosNoPeriodo() {
    var inicio = LocalDateTime.of(2026, 1, 1, 0, 0);
    var fim = LocalDateTime.of(2026, 1, 31, 23, 59, 59);

    var resumo = agendamentoRepository.calcularFaturamentoPorPeriodo(
      inicio,
      fim
    );

    var porCurso = agendamentoRepository.calcularAgendamentosPorCurso(
      inicio,
      fim
    );

    assertEquals(1L, resumo.totalAgendamentos());
    assertEquals(
      0,
      new BigDecimal("100.00").compareTo(resumo.faturamentoTotal())
    );

    assertEquals(1, porCurso.size());
    assertEquals(1L, porCurso.getFirst().quantidadeAgendamentos());
    assertEquals(
      0,
      new BigDecimal("100.00").compareTo(porCurso.getFirst().faturamentoTotal())
    );
  }

  @Test
  void deveriaContarSomenteAlunosAtivos() {
    var resultado = agendamentoRepository.contarAlunosPorCurso();

    assertEquals(1, resultado.size());
    assertEquals(curso.getId(), resultado.getFirst().idCurso());
    assertEquals("Estética", resultado.getFirst().nomeCurso());
    assertEquals(1L, resultado.getFirst().quantidadeAlunos());
  }

  private Usuario criarUsuario(String cpf, String email, boolean ativo) {
    var usuario = new Usuario();
    usuario.setNome("Usuário Teste");
    usuario.setCpf(cpf);
    usuario.setEmail(email);
    usuario.setSenha("senha");
    usuario.setAtivo(ativo);
    usuario.setPerfis(new HashSet<>());

    return entityManager.persist(usuario);
  }

  private void criarAgendamento(
    LocalDateTime data,
    BigDecimal valor,
    StatusAgendamento status
  ) {
    var agendamento = new Agendamento(cliente, aluno, curso, unidade, data);

    agendamento.setValorNoAto(valor);
    agendamento.setStatus(status);
    agendamento.setAtivo(status != StatusAgendamento.CANCELADO);

    entityManager.persist(agendamento);
  }
}
