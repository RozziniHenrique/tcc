package com.tcc.uscs.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tcc.uscs.model.agendamento.StatusAgendamento;
import com.tcc.uscs.model.agendamento.dto.*;
import com.tcc.uscs.service.AgendamentoService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@ActiveProfiles("test")
class AgendamentoControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockitoBean
  private AgendamentoService agendamentoService;

  @Autowired
  private JacksonTester<CadastrarAgendamentoDTO> cadastrarAgendamentoDtoJson;

  @Autowired
  private JacksonTester<DetalharAgendamentoDTO> detalharAgendamentoDtoJson;

  @Autowired
  private JacksonTester<CancelamentoRequestDTO> cancelamentoRequestDtoJson;

  @Autowired
  private JacksonTester<AtualizarAgendamentoDTO> atualizarAgendamentoDtoJson;

  @Test
  @DisplayName(
    "Deveria devolver código HTTP 401 quando requisição não estiver autenticada"
  )
  void cenarioAcessoSemToken() throws Exception {
    mvc.perform(post("/agendamentos")).andExpect(status().isUnauthorized());
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
      1L,
      "Cliente Teste",
      1L,
      "Aluno Teste",
      1L,
      "Curso Teste",
      1L,
      "Unidade Teste",
      List.of(),
      dataValida,
      new BigDecimal("150.00"),
      StatusAgendamento.AGENDADO,
      null
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
      StatusAgendamento.AGENDADO
    );

    var paginaFake = new PageImpl<>(List.of(itemLista));

    when(agendamentoService.listar(any(Pageable.class), any())).thenReturn(
      paginaFake
    );

    mvc.perform(get("/agendamentos")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("Deveria consultar horários disponíveis")
  @WithMockUser(roles = "CLIENTE")
  void cenarioConsultarDisponibilidade() throws Exception {
    var data = LocalDate.of(2026, 10, 5);

    when(agendamentoService.consultarDisponibilidade(1L, data)).thenReturn(
      List.of(new HorarioDisponivelDTO(data.atTime(9, 0), 2L))
    );

    mvc
      .perform(
        get("/agendamentos/disponibilidade")
          .param("idCurso", "1")
          .param("data", data.toString())
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].dataHora").value("2026-10-05T09:00:00"))
      .andExpect(jsonPath("$[0].quantidadeAlunosDisponiveis").value(2));

    verify(agendamentoService).consultarDisponibilidade(1L, data);
  }

  @Test
  @DisplayName("Deveria negar consulta de disponibilidade sem autenticação")
  void cenarioConsultarDisponibilidadeSemAutenticacao() throws Exception {
    mvc
      .perform(
        get("/agendamentos/disponibilidade")
          .param("idCurso", "1")
          .param("data", "2026-10-05")
      )
      .andExpect(status().isUnauthorized());
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
      1L,
      "Cliente Teste",
      1L,
      "Aluno Teste",
      1L,
      "Curso Teste",
      1L,
      "Unidade Teste",
      List.of(),
      dataValida,
      new BigDecimal("150.00"),
      StatusAgendamento.AGENDADO,
      null
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

  @Test
  @DisplayName(
    "Deveria devolver código HTTP 200 ao reagendar com dados válidos"
  )
  @WithMockUser(roles = "CLIENTE")
  void cenarioAtualizarAgendamentoComSucesso() throws Exception {
    var novaData = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0);

    var dados = new AtualizarAgendamentoDTO(1L, 1L, 1L, List.of(1L), novaData);

    var detalhe = new DetalharAgendamentoDTO(
      1L,
      1L,
      "Cliente Teste",
      1L,
      "Aluno Teste",
      1L,
      "Curso Teste",
      1L,
      "Unidade Teste",
      List.of(),
      novaData,
      new BigDecimal("150.00"),
      StatusAgendamento.AGENDADO,
      null
    );

    when(
      agendamentoService.atualizar(
        any(Long.class),
        any(AtualizarAgendamentoDTO.class)
      )
    ).thenReturn(detalhe);

    mvc
      .perform(
        put("/agendamentos/1")
          .contentType(MediaType.APPLICATION_JSON)
          .content(atualizarAgendamentoDtoJson.write(dados).getJson())
      )
      .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Deveria devolver código HTTP 204 ao concluir agendamento")
  @WithMockUser(roles = "ALUNO")
  void cenarioConcluirAgendamentoComSucesso() throws Exception {
    mvc
      .perform(patch("/agendamentos/1/concluir"))
      .andExpect(status().isNoContent());

    verify(agendamentoService).concluir(1L);
  }

  @Test
  @DisplayName("Deveria aceitar filtro de agendamentos por status")
  @WithMockUser(roles = "FUNCIONARIO")
  void cenarioFiltrarAgendamentosPorStatus() throws Exception {
    mvc
      .perform(get("/agendamentos").param("status", "CONCLUIDO"))
      .andExpect(status().isOk());

    verify(agendamentoService).listar(
      any(Pageable.class),
      eq(StatusAgendamento.CONCLUIDO)
    );
  }

  @Test
  @DisplayName("Deveria recusar cadastro com IDs inválidos")
  @WithMockUser(roles = "CLIENTE")
  void cenarioAgendarComIdsInvalidos() throws Exception {
    var dados = new CadastrarAgendamentoDTO(
      -1L,
      1L,
      1L,
      1L,
      List.of(1L),
      LocalDateTime.now().plusDays(1)
    );

    mvc
      .perform(
        post("/agendamentos")
          .contentType(MediaType.APPLICATION_JSON)
          .content(cadastrarAgendamentoDtoJson.write(dados).getJson())
      )
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "CLIENTE")
  void deveriaRecusarJustificativaMuitoLonga() throws Exception {
    var dados = new CancelamentoRequestDTO("a".repeat(256));

    mvc
      .perform(
        delete("/agendamentos/1")
          .contentType(MediaType.APPLICATION_JSON)
          .content(cancelamentoRequestDtoJson.write(dados).getJson())
      )
      .andExpect(status().isBadRequest());
  }
}
