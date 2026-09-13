ALTER TABLE agendamentos
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'AGENDADO';

UPDATE agendamentos
SET status = 'CANCELADO'
WHERE ativo = 0;

ALTER TABLE agendamentos
    ADD COLUMN horario_aluno_bloqueado DATETIME
        GENERATED ALWAYS AS (
            CASE
                WHEN status IN ('AGENDADO', 'CONCLUIDO')
                    THEN data_hora
                ELSE NULL
            END
        ) STORED,
    ADD COLUMN horario_cliente_bloqueado DATETIME
        GENERATED ALWAYS AS (
            CASE
                WHEN status IN ('AGENDADO', 'CONCLUIDO')
                    THEN data_hora
                ELSE NULL
            END
        ) STORED,
    ADD CONSTRAINT uk_agendamento_aluno_horario
        UNIQUE (aluno_id, horario_aluno_bloqueado),
    ADD CONSTRAINT uk_agendamento_cliente_horario
        UNIQUE (cliente_id, horario_cliente_bloqueado);

ALTER TABLE agendamentos
    DROP INDEX uk_agendamento_conflito;

CREATE INDEX idx_agendamentos_status
    ON agendamentos(status);