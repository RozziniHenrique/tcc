package com.tcc.uscs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  @InjectMocks
  private EmailService emailService;

  @Mock
  private JavaMailSender mailSender;

  @Test
  void naoDeveriaEnviarEmailQuandoEstiverDesabilitado() {
    ReflectionTestUtils.setField(emailService, "enabled", false);

    emailService.enviarCodigoRecuperacao("usuario@email.com", "123456", 15);

    verifyNoInteractions(mailSender);
  }

  @Test
  void deveriaEnviarCodigoDeRecuperacao() {
    ReflectionTestUtils.setField(emailService, "enabled", true);
    ReflectionTestUtils.setField(emailService, "from", "no-reply@stfer.com");

    emailService.enviarCodigoRecuperacao("usuario@email.com", "123456", 15);

    var captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(captor.capture());

    var mensagem = captor.getValue();

    assertEquals("no-reply@stfer.com", mensagem.getFrom());
    assertArrayEquals(new String[] { "usuario@email.com" }, mensagem.getTo());
    assertEquals("STFER - Recuperação de senha", mensagem.getSubject());
    assertNotNull(mensagem.getText());
    assertTrue(mensagem.getText().contains("123456"));
    assertTrue(mensagem.getText().contains("15 minutos"));
  }

  @Test
  void deveriaPropagarErroQuandoServidorDeEmailFalhar() {
    ReflectionTestUtils.setField(emailService, "enabled", true);
    ReflectionTestUtils.setField(emailService, "from", "no-reply@stfer.com");

    doThrow(new MailSendException("Servidor indisponível"))
      .when(mailSender)
      .send(any(SimpleMailMessage.class));

    assertThrows(MailSendException.class, () ->
      emailService.enviarCodigoRecuperacao("usuario@email.com", "123456", 15)
    );
  }
}
