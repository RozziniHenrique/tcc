# 💈 Sistema ERP de Agendamento — TCC USCS

> API REST desenvolvida em **Java com Spring Boot 3.5** como Trabalho de Conclusão de Curso na USCS — Universidade Municipal de São Caetano do Sul.

Sistema de gestão integrado para escola de beleza/salão, cobrindo o ciclo completo de operação: **agendamentos, clientes, alunos, funcionários, cursos, serviços e unidades**.

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
- **Controle de acesso por perfil (RBAC)** com três roles: `FUNCIONARIO`, `CLIENTE` e `ALUNO`
- **Stored Procedures** para cadastro transacional de usuários no banco
- **Documentação interativa** via Swagger UI com autenticação JWT integrada
- **Tratamento global de erros** com respostas padronizadas por tipo de exceção
- **Versionamento evolutivo** do banco de dados com Flyway (5 migrations)

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
│   ├── funcionario/     # Inclui enum Funcao: PROFESSOR, ATENDENTE, GESTOR, SUPERVISOR
│   ├── curso/
│   ├── servico/
│   ├── unidade/
│   └── usuario/         # Enum TipoUsuario: CLIENTE, ALUNO, FUNCIONARIO
│
└── infra/
    ├── security/        # JWT Filter, TokenService, SecurityConfigurations, AutenticacaoService
    ├── springdoc/       # SpringDocConfigurations + OpenApiCustomizer global
    ├── exception/       # TratadorDeErros (@RestControllerAdvice) + exceções customizadas
    └── util/            # StoredProcedureHelper
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

| Regra | Detalhe |
|-------|---------|
| Antecedência mínima | Agendamento deve ser criado com pelo menos **30 minutos** de antecedência |
| Horário comercial | Apenas **Seg–Sáb**, das **08h às 19h** |
| Conflito de aluno | Um aluno não pode ter dois agendamentos no mesmo horário |
| Conflito de cliente | Um cliente não pode ter dois agendamentos no mesmo horário |
| Cancelamento | Exige **24h de antecedência** e **justificativa obrigatória** |

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

| Exceção | Status | Situação |
|---------|--------|----------|
| `EntityNotFoundException` | 404 | Entidade não encontrada pelo ID |
| `MethodArgumentNotValidException` | 400 | Falha na validação Jakarta (campos inválidos) |
| `DataIntegrityViolationException` | 400 | Duplicidade detectada pelo JPA |
| `PersistenceException` | 400 ou 500 | Duplicidade ou erro nas Stored Procedures |
| `ValidacaoException` | 400 | Violação de regra de negócio |
| `Exception` | 500 | Erro inesperado (com log) |

---

## 🛠️ Stack Tecnológica

| Categoria | Tecnologia |
|-----------|-----------|
| Linguagem | Java 17+ |
| Framework | Spring Boot 3.5.0 |
| Segurança | Spring Security + JWT (auth0 java-jwt) |
| Persistência | Spring Data JPA + Hibernate |
| Banco de Dados | MySQL 8 |
| Stored Procedures | EntityManager nativo |
| Migrations | Flyway (flyway-core + flyway-mysql) |
| Documentação | SpringDoc OpenAPI (Swagger UI) |
| Validação | Jakarta Validation |
| Build | Maven |
| Utilitários | Lombok |
| Testes | JUnit 5 + Mockito |

---

## 🗄️ Modelo de Dados

```
usuarios (base)
    │
    ├──▶ clientes      (observacoes)
    ├──▶ alunos        (curso_id → cursos)
    └──▶ funcionarios  (funcao: PROFESSOR | ATENDENTE | GESTOR | SUPERVISOR)

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
- `V1` — Criação de todas as tabelas
- `V2` — Stored Procedures para cadastro de Cliente, Funcionário e Aluno
- `V3` — Adição da coluna `justificativa_cancelamento` em agendamentos
- `V4` — Criação da tabela `unidades`
- `V5` — Criação da tabela `servicos`

---

## 🔐 Segurança e Controle de Acesso

Autenticação **stateless** via JWT — sem sessão no servidor. O `SecurityFilter` intercepta cada requisição, valida o token e injeta o usuário no contexto do Spring Security.

### Matriz de Permissões

| Endpoint | PÚBLICO | CLIENTE / ALUNO | FUNCIONARIO |
|----------|---------|-----------------|-------------|
| `POST /login` | ✅ | ✅ | ✅ |
| `POST /clientes` | ✅ | — | ✅ |
| `POST /alunos` | ✅ | — | ✅ |
| `GET /clientes/**`, `GET /alunos/**` | — | ✅ | ✅ |
| `PUT/DELETE /clientes/**`, `/alunos/**` | — | — | ✅ |
| `/agendamentos/**` | — | ✅ | ✅ |
| `/funcionarios/**` | — | — | ✅ |
| `/cursos/**` | — | — | ✅ |
| `GET /swagger-ui/**` | ✅ | ✅ | ✅ |

Senhas armazenadas com **BCrypt** via `BCryptPasswordEncoder`.

---

## 🧪 Testes

Testes unitários implementados com **JUnit 5 + Mockito** cobrindo os cenários críticos do `AgendamentoService`:

| Teste | Cenário |
|-------|---------|
| `cenarioAntecedenciaMinima` | Lança erro ao agendar com menos de 30 min de antecedência |
| `cenarioForaHorarioComercialDomingo` | Lança erro ao agendar em domingo |
| `cenarioConflitoHorarioAluno` | Lança erro quando aluno já tem agendamento no horário |
| `cenarioConflitoHorarioCliente` | Lança erro quando cliente já tem agendamento no horário |
| `cenarioAgendamentoComSucesso` | Cria agendamento e calcula valor total dos serviços corretamente |
| `cenarioCancelarComSucesso` | Cancela agendamento com mais de 24h de antecedência |
| `cenarioCancelarErroAntecedencia` | Lança erro ao cancelar com menos de 24h |
| `cenarioCancelarErroJustificativaEmBranco` | Lança erro ao cancelar sem justificativa |

```bash
# Rodar os testes
mvn test
```

---

## 🚀 Como Executar

### Pré-requisitos
- Java 17+
- Maven 3.8+
- MySQL 8+

### Passos

```bash
# 1. Clone o repositório
git clone https://github.com/RozziniHenrique/tcc.git
cd tcc/uscs

# 2. Crie o banco de dados
mysql -u root -p -e "CREATE DATABASE erp_salao;"

# 3. Configure src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/erp_salao
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

# 4. Execute — o Flyway criará todas as tabelas e procedures automaticamente
mvn spring-boot:run
```

### Documentação interativa (Swagger UI)
```
http://localhost:8080/swagger-ui.html
```
Clique em **Authorize** e insira o token JWT obtido no `POST /login`.

---

## 📊 Endpoints Principais

| Método | Endpoint | Role | Descrição |
|--------|----------|------|-----------|
| `POST` | `/login` | Público | Autenticação — retorna token JWT |
| `POST` | `/clientes` | Público | Cadastro de cliente (via Stored Procedure) |
| `POST` | `/alunos` | Público | Cadastro de aluno (via Stored Procedure) |
| `POST` | `/funcionarios` | FUNCIONARIO | Cadastro de funcionário (via Stored Procedure) |
| `POST` | `/agendamentos` | CLIENTE/ALUNO/FUNC | Cria agendamento com múltiplos serviços |
| `GET` | `/agendamentos` | CLIENTE/ALUNO/FUNC | Lista agendamentos (paginado, ordenado por data desc) |
| `GET` | `/agendamentos/{id}` | CLIENTE/ALUNO/FUNC | Detalha agendamento |
| `DELETE` | `/agendamentos/{id}` | CLIENTE/ALUNO/FUNC | Cancela agendamento (exige justificativa) |
| `GET/POST/PUT/DELETE` | `/cursos/**` | FUNCIONARIO | Gestão de cursos |
| `GET/POST/PUT/DELETE` | `/servicos/**` | FUNCIONARIO | Gestão de serviços |
| `GET/POST/PUT/DELETE` | `/unidades/**` | FUNCIONARIO | Gestão de unidades |

---

## 👨‍💻 Autor

**Henrique Rossini** — Desenvolvedor Backend Java Júnior

[![LinkedIn](https://img.shields.io/badge/LinkedIn-hrossini-blue?style=flat&logo=linkedin)](https://linkedin.com/in/hrossini)
[![GitHub](https://img.shields.io/badge/GitHub-RozziniHenrique-black?style=flat&logo=github)](https://github.com/RozziniHenrique)

---

> Projeto acadêmico em andamento — TCC do curso de Análise e Desenvolvimento de Sistemas (ADS), USCS. Previsão de conclusão: junho/2027.
