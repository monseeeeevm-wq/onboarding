DROP TABLE usuarios;

CREATE TABLE usuarios (
    id                  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cliente_id          INTEGER,
    fecha_nacimiento    DATE         NOT NULL,
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP,
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    rol                 VARCHAR(20)  NOT NULL DEFAULT 'USUARIO',
    telefono            CHAR(10)     NOT NULL,
    password            CHAR(60)     NOT NULL,
    nombre              VARCHAR(50)  NOT NULL,
    apellidos           VARCHAR(100) NOT NULL,
    correo              VARCHAR(100) NOT NULL,
    CONSTRAINT fk_usuarios_cliente    FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT uq_usuarios_cliente    UNIQUE (cliente_id),
    CONSTRAINT ck_usuarios_rol        CHECK (rol IN ('ADMIN','USUARIO')),
    CONSTRAINT ck_usuarios_telefono   CHECK (telefono ~ '^[0-9]{10}$'),
    CONSTRAINT ck_usuarios_nacimiento CHECK (fecha_nacimiento <= CURRENT_DATE)
);

CREATE UNIQUE INDEX ux_usuarios_correo ON usuarios (LOWER(correo));
CREATE INDEX ix_usuarios_activo ON usuarios (activo);
CREATE INDEX ix_usuarios_rol    ON usuarios (rol);