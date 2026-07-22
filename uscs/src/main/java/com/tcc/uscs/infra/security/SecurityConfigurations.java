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
        // 1. Rotas públicas (Login, Cadastros iniciais e Recuperação de Senha)
        req.requestMatchers(HttpMethod.POST, "/login").permitAll();
        req.requestMatchers(HttpMethod.POST, "/alunos").permitAll();
        req.requestMatchers(HttpMethod.POST, "/clientes").permitAll();
        req.requestMatchers(HttpMethod.POST, "/senha/**").permitAll();

        // Documentação Swagger
        req
          .requestMatchers(
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
          )
          .permitAll();

        // 2. Regras Administrativas e do Módulo de Relatórios (RF06)
        req
          .requestMatchers(HttpMethod.POST, "/funcionarios")
          .hasRole("FUNCIONARIO");
        req.requestMatchers("/cursos/**").hasRole("FUNCIONARIO");
        req.requestMatchers("/funcionarios/**").hasRole("FUNCIONARIO");
        req.requestMatchers("/relatorios/**").hasRole("FUNCIONARIO");

        // Gestão de Serviços e Unidades (Apenas Funcionário altera, todos leem)
        req
          .requestMatchers(HttpMethod.GET, "/servicos/**", "/unidades/**")
          .hasAnyRole("FUNCIONARIO", "CLIENTE", "ALUNO");
        req
          .requestMatchers("/servicos/**", "/unidades/**")
          .hasRole("FUNCIONARIO");

        // 3. Módulo de Avaliações (RF09)
        req
          .requestMatchers(HttpMethod.POST, "/avaliacoes/**")
          .hasRole("CLIENTE");
        req
          .requestMatchers(HttpMethod.GET, "/avaliacoes/**")
          .hasAnyRole("FUNCIONARIO", "CLIENTE", "ALUNO");

        // 4. Perfil de Usuários (Clientes / Alunos)
        req
          .requestMatchers(HttpMethod.GET, "/clientes/**", "/alunos/**")
          .hasAnyRole("FUNCIONARIO", "CLIENTE", "ALUNO");
        req
          .requestMatchers(HttpMethod.PUT, "/clientes/**", "/alunos/**")
          .hasAnyRole("FUNCIONARIO", "CLIENTE", "ALUNO");
        req
          .requestMatchers(HttpMethod.DELETE, "/clientes/**", "/alunos/**")
          .hasRole("FUNCIONARIO");

        // 5. Agendamentos
        req
          .requestMatchers("/agendamentos/**")
          .hasAnyRole("FUNCIONARIO", "CLIENTE", "ALUNO");

        // Qualquer outra requisição precisa estar autenticada
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
