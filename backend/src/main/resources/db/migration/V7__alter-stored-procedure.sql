DROP PROCEDURE IF EXISTS sp_cadastrar_usuario_cliente;

CREATE PROCEDURE sp_cadastrar_usuario_cliente(
    IN p_nome VARCHAR(255), IN p_cpf VARCHAR(11), IN p_email VARCHAR(100), 
    IN p_senha VARCHAR(255), IN p_endereco VARCHAR(255), IN p_telefone VARCHAR(20),
    IN p_observacoes TEXT,
    OUT p_id BIGINT
)
BEGIN
    DECLARE v_usuario_id BIGINT DEFAULT NULL;

    SELECT id INTO v_usuario_id FROM usuarios WHERE cpf = p_cpf;

    IF v_usuario_id IS NULL THEN
        IF EXISTS (SELECT 1 FROM usuarios WHERE email = p_email) THEN 
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Email já cadastrado em outra conta';
        END IF;

        INSERT INTO usuarios (nome, cpf, email, senha, endereco_completo, telefone, ativo)
        VALUES (p_nome, p_cpf, p_email, p_senha, p_endereco, p_telefone, 1);
        
        SET v_usuario_id = LAST_INSERT_ID();
    END IF;

    IF EXISTS (SELECT 1 FROM clientes WHERE id = v_usuario_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Este usuário já possui o perfil de Cliente';
    END IF;

    INSERT IGNORE INTO perfis_usuario (usuario_id, perfil) VALUES (v_usuario_id, 'CLIENTE');

    INSERT INTO clientes (id, observacoes) VALUES (v_usuario_id, p_observacoes);
    
    SET p_id = v_usuario_id;
END;

DROP PROCEDURE IF EXISTS sp_cadastrar_usuario_aluno;

CREATE PROCEDURE sp_cadastrar_usuario_aluno(
    IN p_nome VARCHAR(255), IN p_cpf VARCHAR(11), IN p_email VARCHAR(100), 
    IN p_senha VARCHAR(255), IN p_endereco VARCHAR(255), IN p_telefone VARCHAR(20),
    IN p_curso_id BIGINT,
    OUT p_id BIGINT
)
BEGIN
    DECLARE v_usuario_id BIGINT DEFAULT NULL;

    SELECT id INTO v_usuario_id FROM usuarios WHERE cpf = p_cpf;

    IF v_usuario_id IS NULL THEN
        IF EXISTS (SELECT 1 FROM usuarios WHERE email = p_email) THEN 
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Email já cadastrado em outra conta';
        END IF;

        INSERT INTO usuarios (nome, cpf, email, senha, endereco_completo, telefone, ativo)
        VALUES (p_nome, p_cpf, p_email, p_senha, p_endereco, p_telefone, 1);
        
        SET v_usuario_id = LAST_INSERT_ID();
    END IF;

    IF EXISTS (SELECT 1 FROM alunos WHERE id = v_usuario_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Este usuário já possui o perfil de Aluno';
    END IF;

    INSERT IGNORE INTO perfis_usuario (usuario_id, perfil) VALUES (v_usuario_id, 'ALUNO');
    INSERT INTO alunos (id, curso_id) VALUES (v_usuario_id, p_curso_id);
    
    SET p_id = v_usuario_id;
END;

DROP PROCEDURE IF EXISTS sp_cadastrar_usuario_funcionario;

CREATE PROCEDURE sp_cadastrar_usuario_funcionario(
    IN p_nome VARCHAR(255), IN p_cpf VARCHAR(11), IN p_email VARCHAR(100), 
    IN p_senha VARCHAR(255), IN p_endereco VARCHAR(255), IN p_telefone VARCHAR(20),
    IN p_funcao VARCHAR(100),
    OUT p_id BIGINT
)
BEGIN
    DECLARE v_usuario_id BIGINT DEFAULT NULL;

    SELECT id INTO v_usuario_id FROM usuarios WHERE cpf = p_cpf;

    IF v_usuario_id IS NULL THEN
        IF EXISTS (SELECT 1 FROM usuarios WHERE email = p_email) THEN 
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Email já cadastrado em outra conta';
        END IF;

        INSERT INTO usuarios (nome, cpf, email, senha, endereco_completo, telefone, ativo)
        VALUES (p_nome, p_cpf, p_email, p_senha, p_endereco, p_telefone, 1);
        
        SET v_usuario_id = LAST_INSERT_ID();
    END IF;

    IF EXISTS (SELECT 1 FROM funcionarios WHERE id = v_usuario_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Este usuário já possui o perfil de Funcionário';
    END IF;

    INSERT IGNORE INTO perfis_usuario (usuario_id, perfil) VALUES (v_usuario_id, 'FUNCIONARIO');
    INSERT INTO funcionarios (id, funcao) VALUES (v_usuario_id, p_funcao);
    
    SET p_id = v_usuario_id;
END;