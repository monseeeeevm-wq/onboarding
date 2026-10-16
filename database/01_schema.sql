-- ===== CLIENTES =====
CREATE TABLE clientes (
    id                  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fecha_nacimiento    DATE           NOT NULL,
    fecha_registro      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP,
    ingreso_mensual     NUMERIC(12,2)  NOT NULL,
    activo              BOOLEAN        NOT NULL DEFAULT TRUE,
    sexo                CHAR(1)        NOT NULL,
    estado_civil        CHAR(1)        NOT NULL,
    curp                CHAR(18)       NOT NULL,
    rfc                 VARCHAR(13)    NOT NULL,
    telefono_movil      CHAR(10)       NOT NULL,
    telefono_alterno    CHAR(10),
    nombre              VARCHAR(50)    NOT NULL,
    segundo_nombre      VARCHAR(50),
    apellido_paterno    VARCHAR(50)    NOT NULL,
    apellido_materno    VARCHAR(50)    NOT NULL,
    nacionalidad        VARCHAR(50)    NOT NULL,
    correo              VARCHAR(100)   NOT NULL,
    ocupacion           VARCHAR(80)    NOT NULL,
    empresa             VARCHAR(100)   NOT NULL,
    CONSTRAINT ck_clientes_sexo         CHECK (sexo IN ('M','F')),
    CONSTRAINT ck_clientes_estado_civil CHECK (estado_civil IN ('S','C','D','V','U')),
    CONSTRAINT ck_clientes_rfc          CHECK (char_length(rfc) IN (12,13)),
    CONSTRAINT ck_clientes_tel_movil    CHECK (telefono_movil ~ '^[0-9]{10}$'),
    CONSTRAINT ck_clientes_tel_alterno  CHECK (telefono_alterno IS NULL OR telefono_alterno ~ '^[0-9]{10}$'),
    CONSTRAINT ck_clientes_ingreso      CHECK (ingreso_mensual > 0),
    CONSTRAINT ck_clientes_nacimiento   CHECK (fecha_nacimiento <= CURRENT_DATE)
);

CREATE UNIQUE INDEX ux_clientes_curp   ON clientes (curp);
CREATE UNIQUE INDEX ux_clientes_rfc    ON clientes (rfc);
CREATE UNIQUE INDEX ux_clientes_correo ON clientes (LOWER(correo));
CREATE INDEX ix_clientes_fecha_registro ON clientes (fecha_registro);
CREATE INDEX ix_clientes_activo         ON clientes (activo);

-- ===== DOMICILIOS (1:1 con clientes) =====
CREATE TABLE domicilios (
    id              INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cliente_id      INTEGER      NOT NULL,
    codigo_postal   CHAR(5)      NOT NULL,
    numero_exterior VARCHAR(10)  NOT NULL,
    numero_interior VARCHAR(10),
    calle           VARCHAR(100) NOT NULL,
    colonia         VARCHAR(80)  NOT NULL,
    municipio       VARCHAR(80)  NOT NULL,
    estado          VARCHAR(50)  NOT NULL,
    pais            VARCHAR(50)  NOT NULL DEFAULT 'México',
    CONSTRAINT fk_domicilios_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT uq_domicilios_cliente UNIQUE (cliente_id),
    CONSTRAINT ck_domicilios_cp CHECK (codigo_postal ~ '^[0-9]{5}$')
);

-- ===== CUENTAS (1:N con clientes) =====
CREATE TABLE cuentas (
    id             INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cliente_id     INTEGER       NOT NULL,
    saldo          NUMERIC(14,2) NOT NULL DEFAULT 0,
    fecha_apertura TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    numero_cuenta  CHAR(10)      NOT NULL,
    estatus        VARCHAR(10)   NOT NULL DEFAULT 'ACTIVA',
    CONSTRAINT fk_cuentas_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT uq_cuentas_numero  UNIQUE (numero_cuenta),
    CONSTRAINT ck_cuentas_saldo   CHECK (saldo >= 0),
    CONSTRAINT ck_cuentas_numero  CHECK (numero_cuenta ~ '^[0-9]{10}$'),
    CONSTRAINT ck_cuentas_estatus CHECK (estatus IN ('ACTIVA','INACTIVA'))
);

CREATE INDEX ix_cuentas_cliente ON cuentas (cliente_id);
CREATE INDEX ix_cuentas_estatus ON cuentas (estatus);

-- ===== USUARIOS (1:1 con clientes) =====
CREATE TABLE usuarios (
    id                  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cliente_id          INTEGER      NOT NULL,
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP,
    password            CHAR(60)     NOT NULL,
    correo              VARCHAR(100) NOT NULL,
    CONSTRAINT fk_usuarios_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT uq_usuarios_cliente UNIQUE (cliente_id)
);

CREATE UNIQUE INDEX ux_usuarios_correo ON usuarios (LOWER(correo));