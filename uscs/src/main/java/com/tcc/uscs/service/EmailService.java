package com.tcc.uscs.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

  private static final Logger log = LoggerFactory.getLogger(EmailService.class);
  private final JavaMailSender mailSender;

  @Value("${app.mail.enabled:false}")
  private boolean enabled;

  @Value("${app.mail.from:no-reply@stfer.local}")
  private String from;

  public void enviarCodigoRecuperacao(
    String destinatario,
    String codigo,
    long expiracaoMinutos
  ) {
    if (!enabled) {
      log.warn(
        "[DEV] Código de recuperação para {}: {} (expira em {} min)",
        destinatario,
        codigo,
        expiracaoMinutos
      );
      return;
    }

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(destinatario);
    message.setSubject("STFER - Recuperação de senha");
    message.setText(
      "Seu código de recuperação é: " +
        codigo +
        "\nEle expira em " +
        expiracaoMinutos +
        " minutos.\n" +
        "Se você não solicitou a recuperação, ignore esta mensagem."
    );
    mailSender.send(message);
  }
}
