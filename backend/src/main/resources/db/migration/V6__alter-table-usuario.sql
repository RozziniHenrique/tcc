CREATE TABLE perfis_usuario (
    usuario_id BIGINT NOT NULL,
    perfil VARCHAR(50) NOT NULL,
    PRIMARY KEY (usuario_id, perfil),
    CONSTRAINT fk_perfis_usuario_id FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

INSERT INTO perfis_usuario (usuario_id, perfil)
SELECT id, tipo_usuario FROM usuarios;

ALTER TABLE usuarios DROP COLUMN tipo_usuario;