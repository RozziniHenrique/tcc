package com.tcc.uscs.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcc.uscs.model.curso.dto.AtualizarCursoDTO;
import com.tcc.uscs.model.curso.dto.CadastrarCursoDTO;
import com.tcc.uscs.model.curso.dto.DetalharCursoDTO;
import com.tcc.uscs.model.curso.dto.ListarCursoDTO;
import com.tcc.uscs.service.CursoService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CursoControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private CursoService service;

  private DetalharCursoDTO detalhe() {
    return new DetalharCursoDTO(
      1L,
      "Cabeleireiro",
      "Curso profissionalizante",
      "Noturno",
      "120 horas",
      "2026",
      new BigDecimal("1500.00"),
      true
    );
  }

  @Test
  void deveriaRecusarUsuarioNaoAutenticado() throws Exception {
    mvc.perform(get("/cursos")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "CLIENTE")
  void deveriaPermitirListagemParaUsuarioAutenticado() throws Exception {
    var item = new ListarCursoDTO(
      1L,
      "Cabeleireiro",
      "Noturno",
      "120 horas",
      "2026",
      new BigDecimal("1500.00"),
      true
    );

    when(service.listar(any(Pageable.class))).thenReturn(
      new PageImpl<>(List.of(item))
    );

    mvc.perform(get("/cursos")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "PROFESSOR")
  void deveriaCadastrarCursoComoProfessor() throws Exception {
    var dados = new CadastrarCursoDTO(
      "Cabeleireiro",
      "Curso profissionalizante",
      "Noturno",
      "120 horas",
      "2026",
      new BigDecimal("1500.00")
    );

    when(service.cadastrar(any(CadastrarCursoDTO.class))).thenReturn(detalhe());

    mvc
      .perform(
        post("/cursos")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(dados))
      )
      .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser(roles = "PROFESSOR")
  void deveriaRecusarCadastroComDadosInvalidos() throws Exception {
    mvc
      .perform(
        post("/cursos").contentType(MediaType.APPLICATION_JSON).content("{}")
      )
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "CLIENTE")
  void deveriaProibirCadastroPorCliente() throws Exception {
    mvc
      .perform(
        post("/cursos").contentType(MediaType.APPLICATION_JSON).content("{}")
      )
      .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "GESTOR")
  void deveriaAtualizarCursoComoGestor() throws Exception {
    var dados = new AtualizarCursoDTO(
      "Cabeleireiro Avançado",
      null,
      null,
      null,
      null,
      new BigDecimal("1800.00")
    );

    when(
      service.atualizar(any(Long.class), any(AtualizarCursoDTO.class))
    ).thenReturn(detalhe());

    mvc
      .perform(
        put("/cursos/1")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(dados))
      )
      .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deveriaExcluirCursoComoAdministrador() throws Exception {
    mvc.perform(delete("/cursos/1")).andExpect(status().isNoContent());

    verify(service).excluir(1L);
  }
}
