package com.tcc.uscs.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tcc.uscs.model.relatorio.dto.AgendamentosPorCursoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.AlunosPorCursoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.FaturamentoRelatorioDTO;
import com.tcc.uscs.model.relatorio.dto.RelatorioCompletoDTO;
import com.tcc.uscs.service.RelatorioExportacaoService;
import com.tcc.uscs.service.RelatorioPdfService;
import com.tcc.uscs.service.RelatorioService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
class RelatorioControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockitoBean
  private RelatorioService relatorioService;

  @MockitoBean
  private RelatorioExportacaoService relatorioExportacaoService;

  @MockitoBean
  private RelatorioPdfService relatorioPdfService;

  private final LocalDate inicio = LocalDate.of(2026, 1, 1);
  private final LocalDate fim = LocalDate.of(2026, 1, 31);

  @Test
  void deveriaNegarAcessoSemAutenticacao() throws Exception {
    mvc
      .perform(
        get("/relatorios")
          .param("inicio", inicio.toString())
          .param("fim", fim.toString())
      )
      .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "CLIENTE")
  void deveriaNegarAcessoParaCliente() throws Exception {
    mvc
      .perform(
        get("/relatorios")
          .param("inicio", inicio.toString())
          .param("fim", fim.toString())
      )
      .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deveriaGerarRelatorioCompleto() throws Exception {
    var resumo = new FaturamentoRelatorioDTO(2L, new BigDecimal("300.00"));

    var alunos = List.of(new AlunosPorCursoRelatorioDTO(1L, "Estética", 5L));

    var agendamentos = List.of(
      new AgendamentosPorCursoRelatorioDTO(
        1L,
        "Estética",
        2L,
        new BigDecimal("300.00")
      )
    );

    when(relatorioService.gerarRelatorioCompleto(inicio, fim)).thenReturn(
      new RelatorioCompletoDTO(inicio, fim, resumo, alunos, agendamentos)
    );

    mvc
      .perform(
        get("/relatorios")
          .param("inicio", inicio.toString())
          .param("fim", fim.toString())
      )
      .andExpect(status().isOk());

    verify(relatorioService).gerarRelatorioCompleto(inicio, fim);
  }

  @Test
  @WithMockUser(roles = "GESTOR")
  void deveriaGerarFaturamento() throws Exception {
    when(relatorioService.gerarRelatorioFaturamento(inicio, fim)).thenReturn(
      new FaturamentoRelatorioDTO(2L, new BigDecimal("300.00"))
    );

    mvc
      .perform(
        get("/relatorios/faturamento")
          .param("inicio", inicio.toString())
          .param("fim", fim.toString())
      )
      .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "SUPERVISOR")
  void deveriaListarAlunosPorCurso() throws Exception {
    when(relatorioService.gerarRelatorioAlunosPorCurso()).thenReturn(
      List.of(new AlunosPorCursoRelatorioDTO(1L, "Estética", 5L))
    );

    mvc.perform(get("/relatorios/alunos-por-curso")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deveriaListarAgendamentosPorCurso() throws Exception {
    when(
      relatorioService.gerarRelatorioAgendamentosPorCurso(inicio, fim)
    ).thenReturn(
      List.of(
        new AgendamentosPorCursoRelatorioDTO(
          1L,
          "Estética",
          2L,
          new BigDecimal("300.00")
        )
      )
    );

    mvc
      .perform(
        get("/relatorios/agendamentos-por-curso")
          .param("inicio", inicio.toString())
          .param("fim", fim.toString())
      )
      .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deveriaRecusarRelatorioSemDatas() throws Exception {
    mvc.perform(get("/relatorios")).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deveriaBaixarRelatorioCsv() throws Exception {
    var arquivo = "conteudo-csv".getBytes(StandardCharsets.UTF_8);

    when(relatorioExportacaoService.gerarCsv(inicio, fim)).thenReturn(arquivo);

    mvc
      .perform(
        get("/relatorios/exportar/csv")
          .param("inicio", inicio.toString())
          .param("fim", fim.toString())
      )
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith("text/csv"))
      .andExpect(
        header().string(
          HttpHeaders.CONTENT_DISPOSITION,
          "attachment; filename=\"relatorio-stfer-2026-01-01-2026-01-31.csv\""
        )
      )
      .andExpect(content().bytes(arquivo));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deveriaBaixarRelatorioPdf() throws Exception {
    var arquivo = "%PDF-arquivo".getBytes(StandardCharsets.UTF_8);

    when(relatorioPdfService.gerarPdf(inicio, fim)).thenReturn(arquivo);

    mvc
      .perform(
        get("/relatorios/exportar/pdf")
          .param("inicio", inicio.toString())
          .param("fim", fim.toString())
      )
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_PDF))
      .andExpect(
        header().string(
          HttpHeaders.CONTENT_DISPOSITION,
          "attachment; filename=\"relatorio-stfer-2026-01-01-2026-01-31.pdf\""
        )
      )
      .andExpect(content().bytes(arquivo));
  }
}
