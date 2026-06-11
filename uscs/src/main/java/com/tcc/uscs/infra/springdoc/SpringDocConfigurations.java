package com.tcc.uscs.infra.springdoc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
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

  /**
   * Customizador global que injeta as respostas de erro padrão em todos os endpoints do sistema.
   */
  @Bean
  public OpenApiCustomizer customerGlobalHeaderOpenApiCustomizer() {
    return openApi ->
      openApi
        .getPaths()
        .values()
        .forEach(pathItem ->
          pathItem
            .readOperations()
            .forEach(operation -> {
              ApiResponses apiResponses = operation.getResponses();
              apiResponses.addApiResponse(
                "400",
                new ApiResponse().description(
                  "Dados de entrada inválidos (Erro de validação)."
                )
              );

              apiResponses.addApiResponse(
                "403",
                new ApiResponse().description(
                  "Acesso negado - Token inválido ou permissão insuficiente."
                )
              );

              apiResponses.addApiResponse(
                "500",
                new ApiResponse().description("Erro interno no servidor.")
              );
            })
        );
  }
}
