package com.tcc.uscs.infra.web;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfigurations {

  @Bean
  public CorsConfigurationSource corsConfigurationSource(
    @Value("${app.cors.allowed-origin-patterns}") List<
      String
    > allowedOriginPatterns
  ) {
    var configuration = new CorsConfiguration();

    configuration.setAllowedOriginPatterns(allowedOriginPatterns);
    configuration.setAllowedMethods(
      List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
    );
    configuration.setAllowedHeaders(
      List.of("Authorization", "Content-Type", "Accept")
    );
    configuration.setExposedHeaders(List.of("Location", "Content-Disposition"));
    configuration.setAllowCredentials(false);
    configuration.setMaxAge(3600L);

    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
