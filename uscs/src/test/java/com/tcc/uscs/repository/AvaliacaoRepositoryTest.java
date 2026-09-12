package com.tcc.uscs.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.tcc.uscs.model.agendamento.Agendamento;
import com.tcc.uscs.model.agendamento.StatusAgendamento;
import com.tcc.uscs.model.aluno.Aluno;
import com.tcc.uscs.model.avaliacao.Avaliacao;
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
    "spring.datasource.url=jdbc:h2:mem:avaliacao_repository_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
    "spring.jpa.hibernate.ddl-auto=create-only",
    "spring.jpa.show-sql=false",
  }
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AvaliacaoRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private AgendamentoRepository agendamentoRepository;

  @Autowired
  private AvaliacaoRepository avaliacaoRepository;

  private Cliente cliente;
  private Cliente outroCliente;
  private Aluno aluno;
  private Curso curso;
  private Unidade unidade;
  private Agendamento pendente;
  private Agendamento avaliado;

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

    cliente = new Cliente(
      criarUsuario("11111111111", "cliente@teste.com"),
      null
    );
    entityManager.persist(cliente);

    outroCliente = new Cliente(
      criarUsuario("22222222222", "outro@teste.com"),
      null
    );
    entityManager.persist(outroCliente);

    aluno = new Aluno(criarUsuario("33333333333", "aluno@teste.com"), curso);
    entityManager.persist(aluno);

    unidade = new Unidade();
    unidade.setNome("Unidade Centro");
    unidade.setEndereco("Rua Teste, 100");
    unidade.setCidade("São Paulo");
    unidade.setEstado("SP");
    unidade.setAtivo(true);
    entityManager.persist(unidade);

    pendente = criarAgendamento(
      cliente,
      LocalDateTime.of(2026, 1, 10, 10, 0),
      StatusAgendamento.CONCLUIDO
    );

    avaliado = criarAgendamento(
      cliente,
      LocalDateTime.of(2026, 1, 11, 10, 0),
      StatusAgendamento.CONCLUIDO
    );

    criarAgendamento(
      cliente,
      LocalDateTime.of(2026, 1, 12, 10, 0),
      StatusAgendamento.AGENDADO
    );

    criarAgendamento(
      outroCliente,
      LocalDateTime.of(2026, 1, 13, 10, 0),
      StatusAgendamento.CONCLUIDO
    );

    entityManager.persist(new Avaliacao(avaliado, 4, "Bom atendimento"));

    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void deveriaListarSomenteAgendamentoConcluidoNaoAvaliadoDoCliente() {
    var resultado = agendamentoRepository.listarPendentesDeAvaliacao(
      cliente.getId()
    );

    assertEquals(1, resultado.size());
    assertEquals(pendente.getId(), resultado.getFirst().idAgendamento());
    assertEquals(aluno.getId(), resultado.getFirst().idAluno());
    assertEquals("Estética", resultado.getFirst().nomeCurso());
  }

  @Test
  void deveriaLocalizarAvaliacaoPeloAgendamento() {
    assertTrue(avaliacaoRepository.existsByAgendamentoId(avaliado.getId()));

    var resultado = avaliacaoRepository.findByAgendamentoId(avaliado.getId());

    assertTrue(resultado.isPresent());
    assertEquals(4, resultado.get().getNota());
    assertEquals("Bom atendimento", resultado.get().getComentario());
  }

  private Usuario criarUsuario(String cpf, String email) {
    var usuario = new Usuario();
    usuario.setNome("Usuário Teste");
    usuario.setCpf(cpf);
    usuario.setEmail(email);
    usuario.setSenha("senha");
    usuario.setAtivo(true);
    usuario.setPerfis(new HashSet<>());

    return entityManager.persist(usuario);
  }

  private Agendamento criarAgendamento(
    Cliente clienteDoAgendamento,
    LocalDateTime data,
    StatusAgendamento status
  ) {
    var agendamento = new Agendamento(
      clienteDoAgendamento,
      aluno,
      curso,
      unidade,
      data
    );

    agendamento.setValorNoAto(new BigDecimal("100.00"));
    agendamento.setStatus(status);
    agendamento.setAtivo(true);

    return entityManager.persist(agendamento);
  }
}
