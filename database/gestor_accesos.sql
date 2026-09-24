-- Base de datos del gestor de accesos (semana6)
CREATE DATABASE IF NOT EXISTS gestor_accesos
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gestor_accesos;

-- Usuarios del sistema: password con BCrypt ($2y$, igual que Laravel)
-- public_key: llave publica RSA (Base64, X.509)
-- private_key_enc: llave privada RSA cifrada con AES-GCM usando una llave
--                  derivada (PBKDF2) de la contrasena del usuario
CREATE TABLE IF NOT EXISTS usuarios (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    usuario         VARCHAR(50)  NOT NULL UNIQUE,
    email           VARCHAR(120) NOT NULL UNIQUE,
    password        CHAR(60)     NOT NULL,
    public_key      TEXT         NOT NULL,
    private_key_enc TEXT         NOT NULL,
    kdf_salt        VARCHAR(64)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Catalogo de tipos de acceso
CREATE TABLE IF NOT EXISTS tipos_acceso (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

INSERT IGNORE INTO tipos_acceso (nombre) VALUES
    ('Correo'), ('Red social'), ('Banco'), ('Servidor / SSH'),
    ('Base de datos'), ('Hosting / Dominio'), ('Aplicacion web'),
    ('Wi-Fi'), ('Streaming'), ('Otro');

-- Accesos (cuentas) de cada usuario: password cifrada con RSA-OAEP (Base64)
CREATE TABLE IF NOT EXISTS accesos (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id   INT          NOT NULL,
    tipo_id      INT          NOT NULL,
    titulo       VARCHAR(100) NOT NULL,
    url          VARCHAR(255) NULL,
    login        VARCHAR(150) NOT NULL,
    password_enc TEXT         NOT NULL,
    notas        TEXT         NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_accesos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_accesos_tipo    FOREIGN KEY (tipo_id)    REFERENCES tipos_acceso(id),
    INDEX idx_accesos_usuario (usuario_id)
) ENGINE=InnoDB;
