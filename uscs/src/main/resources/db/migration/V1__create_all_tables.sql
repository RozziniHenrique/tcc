-- 1. Usuários
CREATE TABLE usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    endereco_completo VARCHAR(255),
    telefone VARCHAR(20),
    tipo_usuario VARCHAR(50) NOT NULL,
    ativo TINYINT NOT NULL,
    
    PRIMARY KEY (id)
);

-- 2. Cursos
CREATE TABLE cursos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    periodo VARCHAR(50),
    duracao VARCHAR(50),
    ano_vigente VARCHAR(10),
    valor DECIMAL(10,2) NOT NULL,
    ativo TINYINT NOT NULL,
    
    PRIMARY KEY (id)
);

-- 3. Clientes
CREATE TABLE clientes (
    id BIGINT NOT NULL,
    observacoes TEXT,
    
    PRIMARY KEY (id),
    CONSTRAINT fk_clientes_usuario FOREIGN KEY (id) REFERENCES usuarios(id)
);

-- 4. Alunos
CREATE TABLE alunos (
    id BIGINT NOT NULL,
    curso_id BIGINT,
    
    PRIMARY KEY (id),
    CONSTRAINT fk_alunos_usuario FOREIGN KEY (id) REFERENCES usuarios(id),
    CONSTRAINT fk_alunos_curso FOREIGN KEY (curso_id) REFERENCES cursos(id)
);

-- 5. Funcionarios
CREATE TABLE funcionarios (
    id BIGINT NOT NULL,
    funcao VARCHAR(50) NOT NULL,
    
    PRIMARY KEY (id),
    CONSTRAINT fk_funcionarios_usuario FOREIGN KEY (id) REFERENCES usuarios(id)
);

-- 4. Tabela de Agendamentos
CREATE TABLE agendamentos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cliente_id BIGINT NOT NULL,
    aluno_id BIGINT NOT NULL,
    curso_id BIGINT NOT NULL,
    data_hora DATETIME NOT NULL,
    valor_no_ato DECIMAL(10,2) NOT NULL,
    ativo TINYINT NOT NULL,
    
    PRIMARY KEY (id),
    CONSTRAINT fk_agendamentos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_agendamentos_aluno FOREIGN KEY (aluno_id) REFERENCES alunos(id),
    CONSTRAINT fk_agendamentos_curso FOREIGN KEY (curso_id) REFERENCES cursos(id),
    CONSTRAINT uk_agendamento_conflito UNIQUE (aluno_id, data_hora)
);