package com.tcc.uscs.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tcc.uscs.model.avaliacao.dto.AvaliacaoPendenteDTO;
import com.tcc.uscs.model.avaliacao.dto.CadastrarAvaliacaoDTO;
import com.tcc.uscs.model.avaliacao.dto.DetalharAvaliacaoDTO;
import com.tcc.uscs.service.AvaliacaoService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AvaliacaoControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockitoBean
  private AvaliacaoService avaliacaoService;

  @Test
  void deveriaNegarAvaliacaoSemAutenticacao() throws Exception {
    mvc
      .perform(
        post("/avaliacoes")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
            {
              "idAgendamento": 10,
              "nota": 5,
              "comentario": "Excelente"
            }
            """
          )
      )
      .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "ALUNO")
  void deveriaNegarAvaliacaoParaQuemNaoForCliente() throws Exception {
    mvc
      .perform(
        post("/avaliacoes")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
            {
              "idAgendamento": 10,
              "nota": 5
            }
            """
          )
      )
      .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "CLIENTE")
  void deveriaCadastrarAvaliacao() throws Exception {
    var detalhe = new DetalharAvaliacaoDTO(
      1L,
      10L,
      5,
      "Excelente",
      LocalDateTime.of(2026, 1, 10, 15, 0)
    );

    when(avaliacaoService.avaliar(any(CadastrarAvaliacaoDTO.class))).thenReturn(
      detalhe
    );

    mvc
      .perform(
        post("/avaliacoes")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
            {
              "idAgendamento": 10,
              "nota": 5,
              "comentario": "Excelente"
            }
            """
          )
      )
      .andExpect(status().isCreated())
      .andExpect(
        header().string(
          HttpHeaders.LOCATION,
          "http://localhost/avaliacoes/agendamento/10"
        )
      )
      .andExpect(jsonPath("$.nota").value(5));

    verify(avaliacaoService).avaliar(any(CadastrarAvaliacaoDTO.class));
  }

  @Test
  @WithMockUser(roles = "CLIENTE")
  void deveriaRecusarNotaForaDoIntervalo() throws Exception {
    mvc
      .perform(
        post("/avaliacoes")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
            {
              "idAgendamento": 10,
              "nota": 6,
              "comentario": "Nota inválida"
            }
            """
          )
      )
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "CLIENTE")
  void deveriaListarAvaliacoesPendentes() throws Exception {
    when(avaliacaoService.listarPendentes()).thenReturn(
      List.of(
        new AvaliacaoPendenteDTO(
          10L,
          2L,
          "Aluno Teste",
          3L,
          "Estética",
          LocalDateTime.of(2026, 1, 10, 14, 0)
        )
      )
    );

    mvc
      .perform(get("/avaliacoes/pendentes"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].idAgendamento").value(10))
      .andExpect(jsonPath("$[0].nomeAluno").value("Aluno Teste"));
  }

  @Test
  @WithMockUser(roles = "CLIENTE")
  void deveriaConsultarAvaliacaoPorAgendamento() throws Exception {
    when(avaliacaoService.buscarPorAgendamento(10L)).thenReturn(
      new DetalharAvaliacaoDTO(
        1L,
        10L,
        5,
        "Excelente",
        LocalDateTime.of(2026, 1, 10, 15, 0)
      )
    );

    mvc
      .perform(get("/avaliacoes/agendamento/10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.idAgendamento").value(10))
      .andExpect(jsonPath("$.nota").value(5));
  }

  @Test
  void deveriaNegarConsultaSemAutenticacao() throws Exception {
    mvc
      .perform(get("/avaliacoes/agendamento/10"))
      .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "CLIENTE")
  void deveriaRecusarIdDeAgendamentoInvalido() throws Exception {
    mvc
      .perform(
        post("/avaliacoes")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
            {
              "idAgendamento": -1,
              "nota": 5
            }
            """
          )
      )
      .andExpect(status().isBadRequest());
  }
}
