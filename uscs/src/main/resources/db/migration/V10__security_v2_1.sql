ALTER TABLE password_reset_tokens
    ADD COLUMN tentativas INT NOT NULL DEFAULT 0;

CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    token_hash VARCHAR(64) NOT NULL,
    usuario_id BIGINT NOT NULL,
    data_expiracao DATETIME NOT NULL,
    revogado BIT(1) NOT NULL DEFAULT 0,
    criado_em DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_usuario ON refresh_tokens(usuario_id);
CREATE INDEX idx_refresh_token_expiracao ON refresh_tokens(data_expiracao);
