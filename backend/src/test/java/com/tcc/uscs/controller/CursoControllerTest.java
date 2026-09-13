package com.tcc.uscs.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcc.uscs.infra.exception.RecursoNaoEncontradoException;
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
    mvc
      .perform(get("/cursos"))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
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

    mvc
      .perform(get("/cursos"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content").isArray())
      .andExpect(jsonPath("$.content.length()").value(1))
      .andExpect(jsonPath("$.page.number").value(0))
      .andExpect(jsonPath("$.page.size").value(1))
      .andExpect(jsonPath("$.page.totalElements").value(1))
      .andExpect(jsonPath("$.page.totalPages").value(1));
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

  @Test
  @WithMockUser(roles = "PROFESSOR")
  void deveriaRetornar400ParaJsonMalformado() throws Exception {
    mvc
      .perform(
        post("/cursos")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"nome\":")
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error").value("MALFORMED_JSON"));
  }

  @Test
  @WithMockUser(roles = "PROFESSOR")
  void deveriaRetornar405ParaMetodoNaoPermitido() throws Exception {
    mvc
      .perform(
        patch("/cursos/1").contentType(MediaType.APPLICATION_JSON).content("{}")
      )
      .andExpect(status().isMethodNotAllowed())
      .andExpect(jsonPath("$.error").value("METHOD_NOT_ALLOWED"));
  }

  @Test
  @WithMockUser(roles = "PROFESSOR")
  void deveriaRetornar415ParaTipoDeConteudoInvalido() throws Exception {
    mvc
      .perform(post("/cursos").contentType(MediaType.TEXT_PLAIN).content("{}"))
      .andExpect(status().isUnsupportedMediaType())
      .andExpect(jsonPath("$.error").value("UNSUPPORTED_MEDIA_TYPE"));
  }

  @Test
  @WithMockUser
  void deveriaRetornar404ParaRotaInexistente() throws Exception {
    mvc
      .perform(get("/rota-inexistente"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.error").value("NOT_FOUND"));
  }

  @Test
  @WithMockUser(roles = "CLIENTE")
  void deveriaRetornar404ParaCursoInexistente() throws Exception {
    when(service.detalhar(999L)).thenThrow(
      new RecursoNaoEncontradoException("Curso não encontrado ou inativo.")
    );

    mvc
      .perform(get("/cursos/999"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.error").value("NOT_FOUND"))
      .andExpect(
        jsonPath("$.message").value("Curso não encontrado ou inativo.")
      );
  }
}
