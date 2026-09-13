package com.tcc.uscs.infra.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
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
  private final SecurityErrorHandler securityErrorHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http)
    throws Exception {
    return http
      .csrf(csrf -> csrf.disable())
      .cors(Customizer.withDefaults())
      .sessionManagement(sm ->
        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .exceptionHandling(ex ->
        ex
          .authenticationEntryPoint(securityErrorHandler)
          .accessDeniedHandler(securityErrorHandler)
      )
      .authorizeHttpRequests(req -> {
        // Rotas públicas
        req
          .requestMatchers(
            HttpMethod.POST,
            "/login",
            "/auth/login",
            "/auth/refresh"
          )
          .permitAll();
        req
          .requestMatchers(HttpMethod.POST, "/auth/password/**", "/senha/**")
          .permitAll();
        req
          .requestMatchers(HttpMethod.POST, "/alunos", "/clientes")
          .permitAll();
        req.requestMatchers("/actuator/health").permitAll();

        // Swagger
        req
          .requestMatchers(
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
          )
          .permitAll();

        // Conta do próprio usuário
        req.requestMatchers("/me/**").authenticated();

        // Funcionários e relatórios: apenas gestão
        req
          .requestMatchers("/relatorios/**")
          .hasAnyRole("GESTOR", "SUPERVISOR", "ADMIN");
        req
          .requestMatchers("/funcionarios/**")
          .hasAnyRole("GESTOR", "SUPERVISOR", "ADMIN");

        // Cursos: leitura para autenticados; escrita para professor/gestão
        req.requestMatchers(HttpMethod.GET, "/cursos/**").authenticated();
        req
          .requestMatchers("/cursos/**")
          .hasAnyRole("PROFESSOR", "GESTOR", "SUPERVISOR", "ADMIN");

        // Serviços e unidades: leitura para autenticados; escrita para operação/gestão
        req
          .requestMatchers(HttpMethod.GET, "/servicos/**", "/unidades/**")
          .authenticated();
        req
          .requestMatchers("/servicos/**", "/unidades/**")
          .hasAnyRole("ATENDENTE", "GESTOR", "SUPERVISOR", "ADMIN");

        // Avaliações
        req
          .requestMatchers(HttpMethod.POST, "/avaliacoes/**")
          .hasRole("CLIENTE");
        req.requestMatchers(HttpMethod.GET, "/avaliacoes/**").authenticated();

        // Listagens globais de pessoas são somente para funcionários autorizados
        req
          .requestMatchers(HttpMethod.GET, "/clientes", "/alunos")
          .hasAnyRole(
            "ATENDENTE",
            "PROFESSOR",
            "GESTOR",
            "SUPERVISOR",
            "ADMIN"
          );

        // Detalhe e edição continuam com validação de posse nos services
        req
          .requestMatchers(HttpMethod.GET, "/clientes/**", "/alunos/**")
          .authenticated();
        req
          .requestMatchers(HttpMethod.PUT, "/clientes/**", "/alunos/**")
          .authenticated();
        req
          .requestMatchers(HttpMethod.DELETE, "/clientes/**", "/alunos/**")
          .hasAnyRole("GESTOR", "SUPERVISOR", "ADMIN");

        // Agenda: regras finas de posse permanecem no service
        req.requestMatchers("/agendamentos/**").authenticated();

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
