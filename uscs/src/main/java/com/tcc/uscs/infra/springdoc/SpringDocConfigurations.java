package com.tcc.uscs.infra.springdoc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfigurations {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
      .components(
        new Components().addSecuritySchemes(
          "bearer-key",
          new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
        )
      )
      .info(
        new Info()
          .title("TCC USCS - Sistema de Agendamento API")
          .description(
            "API Rest para o gerenciamento de clientes, alunos, cursos e agendamentos."
          )
          .version("v6")
      )
      .addSecurityItem(new SecurityRequirement().addList("bearer-key"));
  }
}
