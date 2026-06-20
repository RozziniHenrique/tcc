package com.tcc.uscs.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tcc.uscs.model.agendamento.dto.*;
import com.tcc.uscs.service.AgendamentoService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
class AgendamentoControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private AgendamentoService agendamentoService;

  @Autowired
  private JacksonTester<CadastrarAgendamentoDTO> cadastrarAgendamentoDtoJson;

  @Autowired
  private JacksonTester<DetalharAgendamentoDTO> detalharAgendamentoDtoJson;

  @Autowired
  private JacksonTester<CancelamentoRequestDTO> cancelamentoRequestDtoJson;

  @Test
  @DisplayName(
    "Deveria devolver código HTTP 403 quando requisição não estiver autenticada"
  )
  void cenarioAcessoSemToken() throws Exception {
    mvc.perform(post("/agendamentos")).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName(
    "Deveria devolver código HTTP 400 ao tentar agendar com dados inválidos"
  )
  @WithMockUser(roles = "CLIENTE")
  void cenarioAgendarDadosInvalidos() throws Exception {
    mvc
      .perform(
        post("/agendamentos")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{}")
      )
      .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName(
    "Deveria devolver código HTTP 201 ao cadastrar agendamento com sucesso"
  )
  @WithMockUser(roles = "CLIENTE")
  void cenarioAgendarComSucesso() throws Exception {
    var dataValida = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
    var dtoCadastro = new CadastrarAgendamentoDTO(
      1L,
      1L,
      1L,
      1L,
      List.of(1L),
      dataValida
    );

    var dtoDetalhar = new DetalharAgendamentoDTO(
      1L,
      "Cliente Teste",
      "Aluno Teste",
      "Curso Teste",
      "Unidade Teste",
      dataValida,
      new BigDecimal("150.00")
    );

    when(
      agendamentoService.agendar(any(CadastrarAgendamentoDTO.class))
    ).thenReturn(dtoDetalhar);

    var jsonInput = cadastrarAgendamentoDtoJson.write(dtoCadastro).getJson();

    mvc
      .perform(
        post("/agendamentos")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonInput)
      )
      .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("Deveria devolver código HTTP 200 ao listar agendamentos")
  @WithMockUser(roles = "FUNCIONARIO")
  void cenarioListarAgendamentos() throws Exception {
    var dataValida = LocalDateTime.now().plusDays(1);

    var itemLista = new ListarAgendamentoDTO(
      1L,
      "Cliente Teste",
      "Aluno Teste",
      "Curso Estética",
      "Unidade Centro",
      dataValida,
      new BigDecimal("100.00"),
      true
    );

    var paginaFake = new PageImpl<>(List.of(itemLista));

    when(agendamentoService.listar(any(Pageable.class))).thenReturn(paginaFake);

    mvc.perform(get("/agendamentos")).andExpect(status().isOk());
  }

  @Test
  @DisplayName(
    "Deveria devolver código HTTP 200 ao detalhar um agendamento existente"
  )
  @WithMockUser(roles = "CLIENTE")
  void cenarioDetalharAgendamento() throws Exception {
    var dataValida = LocalDateTime.now().plusDays(1);
    var dtoDetalhar = new DetalharAgendamentoDTO(
      1L,
      "Cliente Teste",
      "Aluno Teste",
      "Curso Teste",
      "Unidade Teste",
      dataValida,
      new BigDecimal("150.00")
    );

    when(agendamentoService.detalhar(1L)).thenReturn(dtoDetalhar);

    mvc.perform(get("/agendamentos/1")).andExpect(status().isOk());
  }

  @Test
  @DisplayName(
    "Deveria devolver código HTTP 24 No Content ao cancelar agendamento com sucesso"
  )
  @WithMockUser(roles = "CLIENTE")
  void cenarioCancelarAgendamentoComSucesso() throws Exception {
    var dtoCancelamento = new CancelamentoRequestDTO(
      "Cliente solicitou mudança de horário"
    );
    var jsonInput = cancelamentoRequestDtoJson.write(dtoCancelamento).getJson();

    mvc
      .perform(
        delete("/agendamentos/1")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonInput)
      )
      .andExpect(status().isNoContent());

    verify(agendamentoService).cancelar(
      1L,
      "Cliente solicitou mudança de horário"
    );
  }

  @Test
  @DisplayName(
    "Deveria devolver código HTTP 400 ao tentar cancelar sem justificativa válida"
  )
  @WithMockUser(roles = "CLIENTE")
  void cenarioCancelarSemJustificativa() throws Exception {
    mvc
      .perform(
        delete("/agendamentos/1")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"justificativa\":\"\"}")
      )
      .andExpect(status().isBadRequest());
  }
}
