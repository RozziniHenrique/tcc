# 💈 STFER API v2.1.0 — Sistema de Gestão e Agendamento

> API REST desenvolvida em **Java 25 com Spring Boot 3.5.13** como Trabalho de Conclusão de Curso na USCS — Universidade Municipal de São Caetano do Sul.

Sistema de gestão integrado para escola de beleza/salão, cobrindo o ciclo completo de operação: **agendamentos, clientes, alunos, funcionários, cursos, serviços e unidades**.

---

> **Versão 2.1.0** — primeira evolução estrutural do TCC, focada em segurança, autenticação e preparação para o futuro app mobile e dashboard gerencial.

### Novidades da v2.1

- Java 25 padronizado no Maven.
- JWT com expiração configurável e segredo obrigatório por variável de ambiente.
- Refresh token rotativo e logout.
- RBAC por função de funcionário (`ATENDENTE`, `PROFESSOR`, `GESTOR`, `SUPERVISOR`, `ADMIN`).
- Listagens de clientes/alunos restritas a funcionários autorizados.
- Novo endpoint `GET /me` e `PUT /me`.
- Recuperação de senha com código de 6 dígitos armazenado apenas como hash.
- Resposta genérica no fluxo de recuperação para reduzir enumeração de usuários.
- Erros REST em formato padronizado.
- Remoção do `StoredProcedureHelper` duplicado.
- Migrations V10 a V12 para segurança, cadastro seguro e controle de status dos agendamentos.
- Paginação com formato JSON estável para integração com Flutter.

---

## 📋 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades](#-funcionalidades)
- [Arquitetura](#-arquitetura)
- [Decisões Técnicas](#-decisões-técnicas)
- [Stack](#-stack-tecnológica)
- [Modelo de Dados](#-modelo-de-dados)
- [Segurança e Controle de Acesso](#-segurança-e-controle-de-acesso)
- [Testes](#-testes)
- [Como Executar](#-como-executar)
- [Endpoints](#-endpoints)

---

## 📌 Sobre o Projeto

O sistema gerencia o fluxo completo de uma escola de beleza com múltiplas unidades: clientes agendam atendimentos com alunos (futuros profissionais), os quais são vinculados a cursos e orientados por funcionários. O agendamento considera múltiplos serviços, calcula o valor total no ato e aplica regras de negócio reais para garantir integridade dos dados.

---

## ✅ Funcionalidades

- **Agendamentos inteligentes** com seleção de múltiplos serviços e cálculo automático do valor total
- **Distribuição aleatória de alunos** disponíveis por curso quando não há preferência do cliente
- **Gestão completa** de clientes, alunos, funcionários, cursos, serviços e unidades (CRUD)
- **Autenticação e autorização** stateless com Spring Security + JWT
- **Controle de acesso (RBAC)** por perfil e função: `CLIENTE`, `ALUNO`, `FUNCIONARIO`, `ATENDENTE`, `PROFESSOR`, `GESTOR`, `SUPERVISOR` e `ADMIN`
- **Stored Procedures** para cadastro transacional de usuários no banco
- **Documentação interativa** via Swagger UI com autenticação JWT integrada
- **Tratamento global de erros** com respostas padronizadas por tipo de exceção
- **Versionamento evolutivo** do banco de dados com Flyway (12 migrations)

---

## 🏗️ Arquitetura

O projeto segue rigorosamente o padrão de **arquitetura em camadas**, com pacotes organizados por domínio:

```
src/main/java/com/tcc/uscs/
│
├── controller/          # Endpoints REST — recebe requisições e delega ao Service
├── service/             # Regras de negócio e orquestração
├── repository/          # Acesso a dados via Spring Data JPA
│
├── model/               # Domínio organizado por entidade
│   ├── agendamento/
│   │   └── dto/         # DTOs segregados por operação (Cadastrar, Listar, Detalhar, Atualizar, Cancelamento)
│   ├── cliente/
│   ├── aluno/
│   ├── funcionario/     # Inclui enum Funcao: PROFESSOR, ATENDENTE, GESTOR, SUPERVISOR, ADMIN
│   ├── curso/
│   ├── servico/
│   ├── unidade/
│   └── usuario/         # Enum TipoUsuario: CLIENTE, ALUNO, FUNCIONARIO
│
└── infra/
    ├── security/        # JWT Filter, TokenService, SecurityConfigurations, AutenticacaoService
    ├── springdoc/       # SpringDocConfigurations + OpenApiCustomizer global
    ├── exception/       # TratadorDeErros (@RestControllerAdvice) + exceções customizadas
    └── helper/          # StoredProcedureHelper
```

---

## 🔧 Decisões Técnicas

### StoredProcedureHelper — eliminação de redundância (DRY)

O cadastro de usuários (Cliente, Aluno, Funcionário) é feito via **Stored Procedures** chamadas com `EntityManager`. Cada procedure compartilha 6 parâmetros comuns (`p_nome`, `p_cpf`, `p_email`, `p_senha`, `p_endereco`, `p_telefone`).

O `StoredProcedureHelper` centraliza o registro e atribuição desses parâmetros, eliminando código repetido a cada chamada:

```java
// Em vez de repetir isso em cada Service:
query.registerStoredProcedureParameter("p_nome", String.class, ParameterMode.IN);
query.registerStoredProcedureParameter("p_cpf",  String.class, ParameterMode.IN);
// ... mais 4 parâmetros ...

// Basta chamar:
StoredProcedureHelper.registrarParametrosComuns(query, nome, cpf, email, senha, endereco, telefone);
```

As Stored Procedures ainda aplicam validação de unicidade internamente (CPF e e-mail), lançando `SIGNAL SQLSTATE '45000'` com mensagem descritiva quando há duplicidade — capturada pelo `TratadorDeErros` via `PersistenceException`.

---

### Regras de Negócio de Agendamento

Implementadas no `AgendamentoService` como métodos de validação privados antes do `save()`:

| Regra               | Detalhe                                                                   |
| ------------------- | ------------------------------------------------------------------------- |
| Antecedência mínima | Agendamento deve ser criado com pelo menos **30 minutos** de antecedência |
| Horário comercial   | Apenas **Seg–Sáb**, das **08h às 19h**                                    |
| Conflito de aluno   | Um aluno não pode ter dois agendamentos no mesmo horário                  |
| Conflito de cliente | Um cliente não pode ter dois agendamentos no mesmo horário                |
| Cancelamento        | Exige **24h de antecedência** e **justificativa obrigatória**             |

---

### OpenApiCustomizer Global

Em vez de anotar cada endpoint individualmente, um `OpenApiCustomizer` itera por todos os paths e injeta automaticamente as respostas de erro padrão (`400`, `403`, `500`) em **cada operação** do Swagger:

```java
@Bean
public OpenApiCustomizer customerGlobalHeaderOpenApiCustomizer() {
    return openApi -> openApi.getPaths().values()
        .forEach(pathItem -> pathItem.readOperations()
            .forEach(operation -> {
                operation.getResponses().addApiResponse("400", ...);
                operation.getResponses().addApiResponse("403", ...);
                operation.getResponses().addApiResponse("500", ...);
            }));
}
```

---

### Tratamento Global de Erros

O `TratadorDeErros` (`@RestControllerAdvice`) mapeia cada tipo de exceção para um status HTTP e payload descritivo:

| Exceção                           | Status     | Situação                                      |
| --------------------------------- | ---------- | --------------------------------------------- |
| `EntityNotFoundException`         | 404        | Entidade não encontrada pelo ID               |
| `MethodArgumentNotValidException` | 400        | Falha na validação Jakarta (campos inválidos) |
| `DataIntegrityViolationException` | 400        | Duplicidade detectada pelo JPA                |
| `PersistenceException`            | 400 ou 500 | Duplicidade ou erro nas Stored Procedures     |
| `ValidacaoException`              | 400        | Violação de regra de negócio                  |
| `Exception`                       | 500        | Erro inesperado (com log)                     |

---

## 🛠️ Stack Tecnológica

| Categoria         | Tecnologia                             |
| ----------------- | -------------------------------------- |
| Linguagem         | Java 25                                |
| Framework         | Spring Boot 3.5.13                     |
| Segurança         | Spring Security + JWT (auth0 java-jwt) |
| Persistência      | Spring Data JPA + Hibernate            |
| Banco de Dados    | MySQL 8                                |
| Stored Procedures | EntityManager nativo                   |
| Migrations        | Flyway (flyway-core + flyway-mysql)    |
| Documentação      | SpringDoc OpenAPI (Swagger UI)         |
| Validação         | Jakarta Validation                     |
| Build             | Maven                                  |
| Utilitários       | Lombok                                 |
| Testes            | JUnit 5 + Mockito                      |

---

## 🗄️ Modelo de Dados

```
usuarios (base)
    │
    ├──▶ clientes      (observacoes)
    ├──▶ alunos        (curso_id → cursos)
    └──▶ funcionarios  (funcao: PROFESSOR | ATENDENTE | GESTOR | SUPERVISOR | ADMIN)

agendamentos
    ├── cliente_id    → clientes
    ├── aluno_id      → alunos
    ├── curso_id      → cursos
    ├── unidade_id    → unidades
    ├── data_hora     (UNIQUE com aluno_id — constraint de conflito no banco)
    └── valor_no_ato  (calculado em runtime: soma dos serviços selecionados)

agendamento_servicos (N:N)
    ├── agendamento_id
    └── servico_id    → servicos (valor DECIMAL 10,2)
```

**Migrations Flyway:**

- `V1` — Criação das tabelas iniciais
- `V2` — Stored Procedures para cadastro de cliente, funcionário e aluno
- `V3` — Adição da justificativa de cancelamento nos agendamentos
- `V4` — Criação da tabela de unidades
- `V5` — Criação das tabelas de serviços e seus relacionamentos
- `V6` — Implementação dos perfis de usuário
- `V7` — Ajustes nas Stored Procedures
- `V8` — Criação dos tokens de recuperação de senha
- `V9` — Criação do sistema de avaliações
- `V10` — Refresh tokens e controle de tentativas de recuperação de senha
- `V11` — Cadastro seguro de usuários, múltiplos perfis e exclusão lógica de clientes, alunos e funcionários
- `V12` — Status dos agendamentos e proteção contra conflitos de horário

---

## 🔐 Segurança e Controle de Acesso

Autenticação **stateless** via JWT — sem sessão no servidor. O `SecurityFilter` intercepta cada requisição, valida o token e injeta o usuário no contexto do Spring Security.

### Matriz de Permissões

| Endpoint                                                | Acesso                                      |
| ------------------------------------------------------- | ------------------------------------------- |
| `POST /login`, `/auth/login`, `/auth/refresh`           | Público                                     |
| `POST /auth/password/**`, `/senha/**`                   | Público                                     |
| `POST /clientes`, `/alunos`                             | Público                                     |
| `GET /me`, `PUT /me`                                    | Usuário autenticado                         |
| `/agendamentos/**`                                      | Usuário autenticado, com validação de posse |
| `GET /clientes/**`, `/alunos/**`                        | Usuário autenticado, com validação de posse |
| `GET /clientes`, `/alunos`                              | Funcionários autorizados                    |
| `DELETE /clientes/**`, `/alunos/**`                     | GESTOR, SUPERVISOR ou ADMIN                 |
| `GET /cursos/**`                                        | Usuário autenticado                         |
| Escrita em `/cursos/**`                                 | PROFESSOR, GESTOR, SUPERVISOR ou ADMIN      |
| `GET /servicos/**`, `/unidades/**`                      | Usuário autenticado                         |
| Escrita em `/servicos/**`, `/unidades/**`               | ATENDENTE, GESTOR, SUPERVISOR ou ADMIN      |
| `POST /avaliacoes/**`                                   | CLIENTE                                     |
| `GET /avaliacoes/**`                                    | Usuário autenticado                         |
| `/funcionarios/**`, `/relatorios/**`                    | GESTOR, SUPERVISOR ou ADMIN                 |
| `/actuator/health`, `/swagger-ui/**`, `/v3/api-docs/**` | Público                                     |

Senhas armazenadas com **BCrypt** via `BCryptPasswordEncoder`.

---

## 🧪 Testes

A aplicação possui **176 testes automatizados**, implementados com **JUnit 5, Mockito, Spring Security Test e H2**.

| Categoria       | Quantidade |
| --------------- | ---------: |
| Controllers     |         44 |
| Services        |        124 |
| Repositories    |          4 |
| Models e perfis |          4 |
| **Total**       |    **176** |

Os testes cobrem:

- autenticação, refresh token e logout;
- recuperação e redefinição de senha;
- cadastro seguro e múltiplos perfis;
- clientes, alunos e funcionários;
- cursos, serviços e unidades;
- criação, alteração, cancelamento e conclusão de agendamentos;
- prevenção de conflitos de horário;
- relatórios em JSON, CSV e PDF;
- avaliações e avaliações pendentes;
- autorização e validação de posse dos recursos;
- serialização estável das respostas paginadas.

```bash
# Windows — Git Bash
./mvnw.cmd clean test

# Linux/macOS
./mvnw clean test
```

---

## 🚀 Como Executar

### Pré-requisitos

- Java 25
- MySQL 8 ou superior
- Git

O Maven não precisa estar instalado, pois o projeto possui Maven Wrapper.

### Passos

```bash
# 1. Clone o repositório
git clone https://github.com/RozziniHenrique/tcc.git
cd tcc/uscs

# 2. Crie o banco de dados
mysql -u root -p -e "CREATE DATABASE tccuscs;"

# 3. Configure as variáveis de ambiente
export DB_USER="root"
export DB_PASSWORD="sua_senha"
export JWT_SECRET="uma-chave-secreta-segura-com-pelo-menos-32-caracteres"

# 4. Execute a aplicação
./mvnw.cmd spring-boot:run
```

No Linux ou macOS, utilize `./mvnw` no lugar de `./mvnw.cmd`.

### Documentação interativa (Swagger UI)

```
http://localhost:8080/swagger-ui.html
```

Clique em **Authorize** e insira o token JWT obtido no `POST /login`.

---

### Paginação

As rotas de listagem retornam paginação em formato JSON estável:

```json
{
  "content": [],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 0,
    "totalPages": 0
  }
}

## 📊 Endpoints Principais

| Método                | Endpoint                       | Acesso                                 | Descrição                                        |
| --------------------- | ------------------------------ | -------------------------------------- | ------------------------------------------------ |
| `POST`                | `/login` ou `/auth/login`      | Público                                | Autentica e retorna access token e refresh token |
| `POST`                | `/auth/refresh`                | Público                                | Renova os tokens                                 |
| `POST`                | `/auth/logout`                 | Autenticado                            | Revoga o refresh token                           |
| `POST`                | `/auth/password/forgot`        | Público                                | Solicita código de recuperação de senha          |
| `POST`                | `/auth/password/verify`        | Público                                | Verifica o código de recuperação                 |
| `POST`                | `/auth/password/reset`         | Público                                | Redefine a senha e revoga sessões existentes     |
| `GET/PUT`             | `/me`                          | Autenticado                            | Consulta ou atualiza o próprio perfil            |
| `POST`                | `/clientes`                    | Público                                | Cadastra um cliente                              |
| `POST`                | `/alunos`                      | Público                                | Cadastra um aluno                                |
| `GET/POST/PUT/DELETE` | `/agendamentos/**`             | Autenticado                            | Gerencia agendamentos com validação de posse     |
| `GET`                 | `/cursos/**`                   | Autenticado                            | Consulta cursos                                  |
| `POST/PUT/DELETE`     | `/cursos/**`                   | PROFESSOR, GESTOR, SUPERVISOR ou ADMIN | Gerencia cursos                                  |
| `GET`                 | `/servicos/**`, `/unidades/**` | Autenticado                            | Consulta serviços e unidades                     |
| `POST/PUT/DELETE`     | `/servicos/**`, `/unidades/**` | ATENDENTE, GESTOR, SUPERVISOR ou ADMIN | Gerencia serviços e unidades                     |
| `POST`                | `/avaliacoes/**`               | CLIENTE                                | Cria avaliações                                  |
| `GET`                 | `/avaliacoes/**`               | Autenticado                            | Consulta avaliações                              |
| `GET/POST/PUT/DELETE` | `/funcionarios/**`             | GESTOR, SUPERVISOR ou ADMIN            | Gerencia funcionários                            |
| `GET`                 | `/relatorios/**`               | GESTOR, SUPERVISOR ou ADMIN            | Consulta relatórios                              |

---

## 👨‍💻 Autor

**Henrique Rossini** — Desenvolvedor Backend Java

[![LinkedIn](https://img.shields.io/badge/LinkedIn-hrossini-blue?style=flat&logo=linkedin)](https://linkedin.com/in/hrossini)
[![GitHub](https://img.shields.io/badge/GitHub-RozziniHenrique-black?style=flat&logo=github)](https://github.com/RozziniHenrique)

---

> Projeto acadêmico em andamento — TCC do curso de Análise e Desenvolvimento de Sistemas (ADS), USCS. Previsão de conclusão: junho/2027.
```
