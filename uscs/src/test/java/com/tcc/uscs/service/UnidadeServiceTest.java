package com.tcc.uscs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.unidade.Unidade;
import com.tcc.uscs.model.unidade.dto.AtualizarUnidadeDTO;
import com.tcc.uscs.model.unidade.dto.CadastrarUnidadeDTO;
import com.tcc.uscs.repository.UnidadeRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class UnidadeServiceTest {

  @Mock
  private UnidadeRepository repository;

  @InjectMocks
  private UnidadeService service;

  @Test
  @DisplayName("Deveria cadastrar unidade")
  void deveriaCadastrarUnidade() {
    var dados = new CadastrarUnidadeDTO(
      "Unidade Centro",
      "Rua Principal, 100",
      "São Paulo",
      "SP"
    );

    var resultado = service.cadastrar(dados);

    verify(repository).save(org.mockito.ArgumentMatchers.any(Unidade.class));
    assertNotNull(resultado);
  }

  @Test
  @DisplayName("Deveria listar somente unidades ativas")
  void deveriaListarUnidadesAtivas() {
    var unidade = mock(Unidade.class);
    var pageable = Pageable.unpaged();

    when(repository.findAllByAtivoTrue(pageable)).thenReturn(
      new PageImpl<>(List.of(unidade))
    );

    var resultado = service.listar(pageable);

    assertEquals(1, resultado.getTotalElements());
    assertFalse(resultado.isEmpty());
  }

  @Test
  @DisplayName("Deveria detalhar unidade ativa")
  void deveriaDetalharUnidadeAtiva() {
    var unidade = mock(Unidade.class);

    when(repository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(unidade));

    var resultado = service.detalhar(1L);

    assertNotNull(resultado);
  }

  @Test
  @DisplayName("Deveria recusar unidade inexistente ou inativa")
  void deveriaRecusarUnidadeInexistenteOuInativa() {
    when(repository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.empty());

    var erro = assertThrows(ValidacaoException.class, () ->
      service.detalhar(1L)
    );

    assertEquals("Unidade não encontrada ou inativa!", erro.getMessage());
  }

  @Test
  @DisplayName("Deveria atualizar unidade ativa")
  void deveriaAtualizarUnidadeAtiva() {
    var unidade = mock(Unidade.class);
    var dados = mock(AtualizarUnidadeDTO.class);

    when(repository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(unidade));

    var resultado = service.atualizar(1L, dados);

    verify(unidade).atualizar(dados);
    assertNotNull(resultado);
  }

  @Test
  @DisplayName("Deveria excluir logicamente unidade ativa")
  void deveriaExcluirUnidadeAtiva() {
    var unidade = mock(Unidade.class);

    when(repository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(unidade));

    service.excluir(1L);

    verify(unidade).excluir();
  }
}
