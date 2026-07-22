CREATE TABLE avaliacoes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    agendamento_id BIGINT NOT NULL UNIQUE,
    nota INT NOT NULL,
    comentario VARCHAR(500),
    data_avaliacao DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_avaliacoes_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamentos(id)
);