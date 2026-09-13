package com.tcc.uscs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tcc.uscs.infra.exception.RecursoNaoEncontradoException;
import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.curso.Curso;
import com.tcc.uscs.model.curso.dto.AtualizarCursoDTO;
import com.tcc.uscs.model.curso.dto.CadastrarCursoDTO;
import com.tcc.uscs.repository.CursoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CursoServiceTest {

  @Mock
  private CursoRepository repository;

  @InjectMocks
  private CursoService service;

  private CadastrarCursoDTO dadosCadastro() {
    return new CadastrarCursoDTO(
      "Cabeleireiro",
      "Curso profissionalizante",
      "Noturno",
      "120 horas",
      "2026",
      new BigDecimal("1500.00")
    );
  }

  private Curso cursoAtivo() {
    var curso = new Curso(dadosCadastro());
    curso.setId(1L);
    return curso;
  }

  @Test
  void deveriaCadastrarCurso() {
    var dados = dadosCadastro();

    var resultado = service.cadastrar(dados);

    assertNotNull(resultado);
    assertEquals("Cabeleireiro", resultado.nome());
    assertTrue(resultado.ativo());
    verify(repository).save(any(Curso.class));
  }

  @Test
  void deveriaRecusarCursoDuplicado() {
    var dados = dadosCadastro();

    when(
      repository.existsByNomeIgnoreCaseAndPeriodoIgnoreCaseAndAnoVigenteAndAtivoTrue(
        dados.nome(),
        dados.periodo(),
        dados.anoVigente()
      )
    ).thenReturn(true);

    var erro = assertThrows(ValidacaoException.class, () ->
      service.cadastrar(dados)
    );

    assertEquals(
      "Já existe um curso ativo com o mesmo nome, período e ano vigente.",
      erro.getMessage()
    );
    verify(repository, never()).save(any());
  }

  @Test
  void deveriaListarSomenteCursosAtivos() {
    var paginacao = Pageable.unpaged();
    when(repository.findAllByAtivoTrue(paginacao)).thenReturn(
      new PageImpl<>(List.of(cursoAtivo()))
    );

    var resultado = service.listar(paginacao);

    assertEquals(1, resultado.getTotalElements());
    assertEquals("Cabeleireiro", resultado.getContent().getFirst().nome());
  }

  @Test
  void deveriaDetalharCursoAtivo() {
    when(repository.findByIdAndAtivoTrue(1L)).thenReturn(
      Optional.of(cursoAtivo())
    );

    var resultado = service.detalhar(1L);

    assertEquals(1L, resultado.id());
    assertEquals("Cabeleireiro", resultado.nome());
  }

  @Test
  void deveriaRecusarCursoInexistenteOuInativo() {
    when(repository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.empty());

    var erro = assertThrows(RecursoNaoEncontradoException.class, () ->
      service.detalhar(1L)
    );

    assertEquals("Curso não encontrado ou inativo.", erro.getMessage());
  }

  @Test
  void deveriaAtualizarCurso() {
    var curso = cursoAtivo();
    var dados = new AtualizarCursoDTO(
      "Cabeleireiro Avançado",
      null,
      null,
      null,
      null,
      new BigDecimal("1800.00")
    );

    when(repository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(curso));

    var resultado = service.atualizar(1L, dados);

    assertEquals("Cabeleireiro Avançado", resultado.nome());
    assertEquals(new BigDecimal("1800.00"), resultado.valor());
  }

  @Test
  void deveriaRecusarDuplicidadeAoAtualizar() {
    var curso = cursoAtivo();
    var dados = new AtualizarCursoDTO(
      "Barbearia",
      null,
      null,
      null,
      null,
      null
    );

    when(repository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(curso));
    when(
      repository.existsByNomeIgnoreCaseAndPeriodoIgnoreCaseAndAnoVigenteAndAtivoTrueAndIdNot(
        "Barbearia",
        "Noturno",
        "2026",
        1L
      )
    ).thenReturn(true);

    var erro = assertThrows(ValidacaoException.class, () ->
      service.atualizar(1L, dados)
    );

    assertEquals(
      "Já existe um curso ativo com o mesmo nome, período e ano vigente.",
      erro.getMessage()
    );
    assertEquals("Cabeleireiro", curso.getNome());
  }

  @Test
  void deveriaExcluirCursoLogicamente() {
    var curso = cursoAtivo();
    when(repository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(curso));

    service.excluir(1L);

    assertFalse(curso.getAtivo());
  }

  @Test
  void deveriaRecusarAtualizacaoVazia() {
    var dados = new AtualizarCursoDTO(null, null, null, null, null, null);

    var erro = assertThrows(ValidacaoException.class, () ->
      service.atualizar(1L, dados)
    );

    assertEquals(
      "Informe pelo menos um campo para realizar a atualização.",
      erro.getMessage()
    );
    verifyNoInteractions(repository);
  }
}
