-- Procedure para CLIENTE
CREATE PROCEDURE sp_cadastrar_usuario_cliente(
    IN p_nome VARCHAR(255), IN p_cpf VARCHAR(11), IN p_email VARCHAR(100), 
    IN p_senha VARCHAR(255), IN p_endereco VARCHAR(255), IN p_telefone VARCHAR(20),
    OUT p_id BIGINT
)
BEGIN
    IF EXISTS (SELECT 1 FROM usuarios WHERE email = p_email) THEN 
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Email já cadastrado';
    END IF;
    IF EXISTS (SELECT 1 FROM usuarios WHERE cpf = p_cpf) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF já cadastrado';
    END IF;

    INSERT INTO usuarios (nome, cpf, email, senha, endereco_completo, telefone, tipo_usuario, ativo)
    VALUES (p_nome, p_cpf, p_email, p_senha, p_endereco, p_telefone, 'CLIENTE', 1);
    
    SET p_id = LAST_INSERT_ID();
    
    INSERT INTO clientes (id, observacoes) VALUES (p_id, NULL);
END;

-- Procedure para FUNCIONARIO
CREATE PROCEDURE sp_cadastrar_usuario_funcionario(
    IN p_nome VARCHAR(255), IN p_cpf VARCHAR(11), IN p_email VARCHAR(100), 
    IN p_senha VARCHAR(255), IN p_endereco VARCHAR(255), IN p_telefone VARCHAR(20),
    OUT p_id BIGINT
)
BEGIN
    IF EXISTS (SELECT 1 FROM usuarios WHERE email = p_email) THEN 
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Email já cadastrado';
    END IF;
    IF EXISTS (SELECT 1 FROM usuarios WHERE cpf = p_cpf) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF já cadastrado';
    END IF;

    INSERT INTO usuarios (nome, cpf, email, senha, endereco_completo, telefone, tipo_usuario, ativo)
    VALUES (p_nome, p_cpf, p_email, p_senha, p_endereco, p_telefone, 'FUNCIONARIO', 1);
    
    SET p_id = LAST_INSERT_ID();
    
    INSERT INTO funcionarios (id, funcao) VALUES (p_id, 'NÃO DEFINIDA');
END;

-- Procedure para ALUNO
CREATE PROCEDURE sp_cadastrar_usuario_aluno(
    IN p_nome VARCHAR(255), IN p_cpf VARCHAR(11), IN p_email VARCHAR(100), 
    IN p_senha VARCHAR(255), IN p_endereco VARCHAR(255), IN p_telefone VARCHAR(20),
    OUT p_id BIGINT
)
BEGIN
    IF EXISTS (SELECT 1 FROM usuarios WHERE email = p_email) THEN 
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Email já cadastrado';
    END IF;
    IF EXISTS (SELECT 1 FROM usuarios WHERE cpf = p_cpf) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF já cadastrado';
    END IF;

    INSERT INTO usuarios (nome, cpf, email, senha, endereco_completo, telefone, tipo_usuario, ativo)
    VALUES (p_nome, p_cpf, p_email, p_senha, p_endereco, p_telefone, 'ALUNO', 1);
    
    SET p_id = LAST_INSERT_ID();
    
    INSERT INTO alunos (id, curso_id) VALUES (p_id, NULL);
END;