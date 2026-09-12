package com.tcc.uscs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tcc.uscs.infra.exception.ValidacaoException;
import com.tcc.uscs.model.servico.Servico;
import com.tcc.uscs.repository.ServicoRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

  @Mock
  private ServicoRepository repository;

  @InjectMocks
  private ServicoService service;

  @Test
  void deveriaRetornarServicosAtivos() {
    var servico1 = mock(Servico.class);
    var servico2 = mock(Servico.class);
    var ids = List.of(1L, 2L);

    when(repository.findAllByIdInAndAtivoTrue(ids)).thenReturn(
      List.of(servico1, servico2)
    );

    var resultado = service.buscarServicosValidos(ids);

    assertEquals(2, resultado.size());
    assertSame(servico1, resultado.get(0));
    assertSame(servico2, resultado.get(1));
  }

  @Test
  void deveriaRecusarListaNulaOuVazia() {
    var erroListaNula = assertThrows(ValidacaoException.class, () ->
      service.buscarServicosValidos(null)
    );

    var erroListaVazia = assertThrows(ValidacaoException.class, () ->
      service.buscarServicosValidos(List.of())
    );

    assertEquals(
      "A lista de serviços não pode estar vazia.",
      erroListaNula.getMessage()
    );
    assertEquals(
      "A lista de serviços não pode estar vazia.",
      erroListaVazia.getMessage()
    );
    verifyNoInteractions(repository);
  }

  @Test
  void deveriaRecusarIdsDuplicados() {
    var erro = assertThrows(ValidacaoException.class, () ->
      service.buscarServicosValidos(List.of(1L, 1L))
    );

    assertEquals(
      "A lista de serviços não pode conter IDs duplicados.",
      erro.getMessage()
    );
    verifyNoInteractions(repository);
  }

  @Test
  void deveriaRecusarServicoInexistenteOuInativo() {
    var ids = List.of(1L, 2L);

    when(repository.findAllByIdInAndAtivoTrue(ids)).thenReturn(
      List.of(mock(Servico.class))
    );

    var erro = assertThrows(ValidacaoException.class, () ->
      service.buscarServicosValidos(ids)
    );

    assertEquals(
      "Um ou mais serviços informados não foram encontrados ou estão inativos!",
      erro.getMessage()
    );
  }
}
