ALTER TABLE clientes
    ADD COLUMN ativo BIT(1) NOT NULL DEFAULT 1;

ALTER TABLE alunos
    ADD COLUMN ativo BIT(1) NOT NULL DEFAULT 1;

ALTER TABLE funcionarios
    ADD COLUMN ativo BIT(1) NOT NULL DEFAULT 1;

DROP PROCEDURE IF EXISTS sp_cadastrar_usuario_cliente;

CREATE PROCEDURE sp_cadastrar_usuario_cliente(
    IN p_nome VARCHAR(255),
    IN p_cpf VARCHAR(11),
    IN p_email VARCHAR(100),
    IN p_senha VARCHAR(255),
    IN p_endereco VARCHAR(255),
    IN p_telefone VARCHAR(20),
    IN p_observacoes TEXT,
    OUT p_id BIGINT
)
BEGIN
    IF EXISTS (SELECT 1 FROM usuarios WHERE cpf = p_cpf) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'CPF já cadastrado';
    END IF;

    IF EXISTS (SELECT 1 FROM usuarios WHERE email = p_email) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Email já cadastrado';
    END IF;

    INSERT INTO usuarios (
        nome, cpf, email, senha, endereco_completo, telefone, ativo
    )
    VALUES (
        p_nome, p_cpf, p_email, p_senha, p_endereco, p_telefone, 1
    );

    SET p_id = LAST_INSERT_ID();

    INSERT INTO perfis_usuario (usuario_id, perfil)
    VALUES (p_id, 'CLIENTE');

    INSERT INTO clientes (id, observacoes)
    VALUES (p_id, p_observacoes);
END;

DROP PROCEDURE IF EXISTS sp_cadastrar_usuario_aluno;

CREATE PROCEDURE sp_cadastrar_usuario_aluno(
    IN p_nome VARCHAR(255),
    IN p_cpf VARCHAR(11),
    IN p_email VARCHAR(100),
    IN p_senha VARCHAR(255),
    IN p_endereco VARCHAR(255),
    IN p_telefone VARCHAR(20),
    IN p_curso_id BIGINT,
    OUT p_id BIGINT
)
BEGIN
    IF EXISTS (SELECT 1 FROM usuarios WHERE cpf = p_cpf) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'CPF já cadastrado';
    END IF;

    IF EXISTS (SELECT 1 FROM usuarios WHERE email = p_email) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Email já cadastrado';
    END IF;

    INSERT INTO usuarios (
        nome, cpf, email, senha, endereco_completo, telefone, ativo
    )
    VALUES (
        p_nome, p_cpf, p_email, p_senha, p_endereco, p_telefone, 1
    );

    SET p_id = LAST_INSERT_ID();

    INSERT INTO perfis_usuario (usuario_id, perfil)
    VALUES (p_id, 'ALUNO');

    INSERT INTO alunos (id, curso_id)
    VALUES (p_id, p_curso_id);
END;

DROP PROCEDURE IF EXISTS sp_cadastrar_usuario_funcionario;

CREATE PROCEDURE sp_cadastrar_usuario_funcionario(
    IN p_nome VARCHAR(255),
    IN p_cpf VARCHAR(11),
    IN p_email VARCHAR(100),
    IN p_senha VARCHAR(255),
    IN p_endereco VARCHAR(255),
    IN p_telefone VARCHAR(20),
    IN p_funcao VARCHAR(100),
    OUT p_id BIGINT
)
BEGIN
    IF EXISTS (SELECT 1 FROM usuarios WHERE cpf = p_cpf) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'CPF já cadastrado';
    END IF;

    IF EXISTS (SELECT 1 FROM usuarios WHERE email = p_email) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Email já cadastrado';
    END IF;

    INSERT INTO usuarios (
        nome, cpf, email, senha, endereco_completo, telefone, ativo
    )
    VALUES (
        p_nome, p_cpf, p_email, p_senha, p_endereco, p_telefone, 1
    );

    SET p_id = LAST_INSERT_ID();

    INSERT INTO perfis_usuario (usuario_id, perfil)
    VALUES (p_id, 'FUNCIONARIO');

    INSERT INTO funcionarios (id, funcao)
    VALUES (p_id, p_funcao);
END;