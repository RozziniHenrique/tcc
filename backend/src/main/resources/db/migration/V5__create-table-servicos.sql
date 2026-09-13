CREATE TABLE servicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    descricao TEXT,
    valor DECIMAL(10,2) NOT NULL,
    ativo BIT(1) NOT NULL DEFAULT 1
);

CREATE TABLE agendamento_servicos (
    agendamento_id BIGINT NOT NULL,
    servico_id BIGINT NOT NULL,
    PRIMARY KEY (agendamento_id, servico_id),
    CONSTRAINT fk_agendamento_servicos_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamentos(id) ON DELETE CASCADE,
    CONSTRAINT fk_agendamento_servicos_servico FOREIGN KEY (servico_id) REFERENCES servicos(id)
);