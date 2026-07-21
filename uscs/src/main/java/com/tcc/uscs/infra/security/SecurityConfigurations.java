package com.tcc.uscs.infra.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfigurations {

  private final SecurityFilter securityFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http)
    throws Exception {
    return http
      .csrf(csrf -> csrf.disable())
      .sessionManagement(sm ->
        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .authorizeHttpRequests(req -> {
        // 1. Rotas públicas
        req.requestMatchers(HttpMethod.POST, "/login").permitAll();
        req.requestMatchers(HttpMethod.POST, "/alunos").permitAll();
        req.requestMatchers(HttpMethod.POST, "/clientes").permitAll();
        req.requestMatchers(HttpMethod.POST, "/senha/**").permitAll();

        // Swagger
        req
          .requestMatchers(
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
          )
          .permitAll();

        // 2. Trava Segurança Funcionario
        req
          .requestMatchers(HttpMethod.POST, "/funcionarios")
          .hasRole("FUNCIONARIO");
        req.requestMatchers("/cursos/**").hasRole("FUNCIONARIO");
        req.requestMatchers("/funcionarios/**").hasRole("FUNCIONARIO");
        req
          .requestMatchers(HttpMethod.GET, "/servicos/**", "/unidades/**")
          .hasAnyRole("FUNCIONARIO", "CLIENTE", "ALUNO");
        req
          .requestMatchers("/servicos/**", "/unidades/**")
          .hasRole("FUNCIONARIO");
        req.requestMatchers("/relatorios/**").hasRole("FUNCIONARIO");

        // 3. Rotas de Alunos, Clientes e Agendamentos
        req
          .requestMatchers(HttpMethod.GET, "/clientes/**", "/alunos/**")
          .hasAnyRole("FUNCIONARIO", "CLIENTE", "ALUNO");

        req
          .requestMatchers(HttpMethod.PUT, "/clientes/**", "/alunos/**")
          .hasAnyRole("FUNCIONARIO", "CLIENTE", "ALUNO");

        req
          .requestMatchers(HttpMethod.DELETE, "/clientes/**", "/alunos/**")
          .hasRole("FUNCIONARIO");

        req
          .requestMatchers("/agendamentos/**")
          .hasAnyRole("FUNCIONARIO", "CLIENTE", "ALUNO");

        req.anyRequest().authenticated();
      })
      .addFilterBefore(
        securityFilter,
        UsernamePasswordAuthenticationFilter.class
      )
      .build();
  }

  @Bean
  public AuthenticationManager authenticationManager(
    AuthenticationConfiguration configuration
  ) throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
