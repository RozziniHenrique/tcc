package com.tcc.uscs.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tcc.uscs.model.usuario.dto.RedefinirSenhaDTO;
import com.tcc.uscs.model.usuario.dto.SolicitarRecuperacaoSenhaDTO;
import com.tcc.uscs.model.usuario.dto.VerificarCodigoSenhaDTO;
import com.tcc.uscs.service.RecuperacaoSenhaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecuperacaoSenhaControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockitoBean
  private RecuperacaoSenhaService recuperacaoSenhaService;

  @Test
  void deveriaSolicitarRecuperacaoSemAutenticacao() throws Exception {
    mvc
      .perform(
        post("/auth/password/forgot")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
            {
              "email": "usuario@email.com"
            }
            """
          )
      )
      .andExpect(status().isOk())
      .andExpect(
        jsonPath("$.mensagem").value(
          "Se o e-mail estiver cadastrado, um código de recuperação será enviado."
        )
      );

    verify(recuperacaoSenhaService).solicitarRecuperacao(
      any(SolicitarRecuperacaoSenhaDTO.class)
    );
  }

  @Test
  void deveriaRecusarEmailInvalido() throws Exception {
    mvc
      .perform(
        post("/auth/password/forgot")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
            {
              "email": "email-invalido"
            }
            """
          )
      )
      .andExpect(status().isBadRequest());
  }

  @Test
  void deveriaVerificarCodigoValido() throws Exception {
    when(
      recuperacaoSenhaService.verificarCodigo(
        any(VerificarCodigoSenhaDTO.class)
      )
    ).thenReturn(true);

    mvc
      .perform(
        post("/auth/password/verify")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
            {
              "email": "usuario@email.com",
              "codigo": "123456"
            }
            """
          )
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.valido").value(true));
  }

  @Test
  void deveriaRecusarCodigoComFormatoInvalido() throws Exception {
    mvc
      .perform(
        post("/auth/password/verify")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
            {
              "email": "usuario@email.com",
              "codigo": "123"
            }
            """
          )
      )
      .andExpect(status().isBadRequest());
  }

  @Test
  void deveriaRedefinirSenha() throws Exception {
    mvc
      .perform(
        post("/auth/password/reset")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
            {
              "email": "usuario@email.com",
              "codigo": "123456",
              "novaSenha": "novaSenha123",
              "confirmacaoSenha": "novaSenha123"
            }
            """
          )
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.mensagem").value("Senha alterada com sucesso."));

    verify(recuperacaoSenhaService).redefinirSenha(
      any(RedefinirSenhaDTO.class)
    );
  }

  @Test
  void deveriaRecusarSenhaMuitoCurta() throws Exception {
    mvc
      .perform(
        post("/auth/password/reset")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
            {
              "email": "usuario@email.com",
              "codigo": "123456",
              "novaSenha": "123",
              "confirmacaoSenha": "123"
            }
            """
          )
      )
      .andExpect(status().isBadRequest());
  }

  @Test
  void deveriaRecusarConfirmacaoAusente() throws Exception {
    mvc
      .perform(
        post("/auth/password/reset")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
            {
              "email": "usuario@email.com",
              "codigo": "123456",
              "novaSenha": "novaSenha123"
            }
            """
          )
      )
      .andExpect(status().isBadRequest());
  }
}
