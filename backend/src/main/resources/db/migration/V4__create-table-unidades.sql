CREATE TABLE unidades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    endereco VARCHAR(255) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    estado VARCHAR(2) NOT NULL,
    ativo BIT(1) NOT NULL DEFAULT 1
);

ALTER TABLE agendamentos 
ADD COLUMN unidade_id BIGINT,
ADD CONSTRAINT fk_agendamentos_unidade FOREIGN KEY (unidade_id) REFERENCES unidades(id);