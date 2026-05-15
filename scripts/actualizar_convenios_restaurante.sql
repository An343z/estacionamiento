-- Migracion para pagos por convenio/restaurante.
-- Ejecutar una sola vez sobre la base de datos existente.
-- Si tu panel usa otra base por defecto, descomenta y ajusta:
-- USE piuqnwsm_estacionamiento;

ALTER TABLE registros_entrada_salida
ADD COLUMN IF NOT EXISTS promocion_aplicada VARCHAR(255) NULL;

ALTER TABLE convenios_restaurante
ADD COLUMN IF NOT EXISTS tipo_cobertura VARCHAR(30) NOT NULL DEFAULT 'TOTAL',
ADD COLUMN IF NOT EXISTS porcentaje_cobertura DECIMAL(5,2) NOT NULL DEFAULT 100.00,
ADD COLUMN IF NOT EXISTS monto_maximo DECIMAL(10,2) NULL,
ADD COLUMN IF NOT EXISTS horas_gratis INT NOT NULL DEFAULT 0;

ALTER TABLE clientes_restaurante
ADD COLUMN IF NOT EXISTS convenio_id INT NULL,
ADD COLUMN IF NOT EXISTS estacionamiento_id INT NULL,
ADD COLUMN IF NOT EXISTS fecha_fin TIMESTAMP NULL,
ADD COLUMN IF NOT EXISTS observaciones TEXT NULL;

CREATE TABLE IF NOT EXISTS liquidaciones_restaurante (
    id INT PRIMARY KEY AUTO_INCREMENT,
    restaurante_id INT NOT NULL,
    estacionamiento_id INT NOT NULL,
    convenio_id INT NULL,
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_fin TIMESTAMP NOT NULL,
    fecha_liquidacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10,2) NOT NULL DEFAULT 0,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    folio_liquidacion VARCHAR(40) UNIQUE,
    observaciones TEXT,
    FOREIGN KEY (restaurante_id) REFERENCES restaurantes(id),
    FOREIGN KEY (estacionamiento_id) REFERENCES estacionamientos(id),
    FOREIGN KEY (convenio_id) REFERENCES convenios_restaurante(id),
    INDEX idx_liq_restaurante (restaurante_id),
    INDEX idx_liq_estacionamiento (estacionamiento_id),
    INDEX idx_liq_estado (estado),
    INDEX idx_liq_fecha (fecha_liquidacion)
);

CREATE TABLE IF NOT EXISTS pagos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    registro_id INT NOT NULL,
    estacionamiento_id INT NOT NULL,
    cajero_id INT NOT NULL,
    cajero_nombre VARCHAR(200),
    monto DECIMAL(10,2) NOT NULL DEFAULT 0,
    monto_pagado DECIMAL(10,2) NOT NULL DEFAULT 0,
    cambio DECIMAL(10,2) NOT NULL DEFAULT 0,
    metodo_pago VARCHAR(30) NOT NULL,
    numero_ticket VARCHAR(40) UNIQUE,
    fecha_pago TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notas TEXT,
    anulado BOOLEAN DEFAULT FALSE,
    restaurante_id INT NULL,
    convenio_id INT NULL,
    liquidacion_restaurante_id INT NULL,
    estado_liquidacion VARCHAR(20) NOT NULL DEFAULT 'NO_APLICA',
    FOREIGN KEY (registro_id) REFERENCES registros_entrada_salida(id),
    FOREIGN KEY (estacionamiento_id) REFERENCES estacionamientos(id),
    FOREIGN KEY (cajero_id) REFERENCES usuarios(id),
    FOREIGN KEY (restaurante_id) REFERENCES restaurantes(id),
    FOREIGN KEY (convenio_id) REFERENCES convenios_restaurante(id),
    FOREIGN KEY (liquidacion_restaurante_id) REFERENCES liquidaciones_restaurante(id),
    INDEX idx_pagos_estacionamiento (estacionamiento_id),
    INDEX idx_pagos_fecha (fecha_pago),
    INDEX idx_pagos_metodo (metodo_pago),
    INDEX idx_pagos_anulado (anulado),
    INDEX idx_pagos_registro (registro_id),
    INDEX idx_pagos_restaurante (restaurante_id),
    INDEX idx_pagos_liquidacion_estado (estado_liquidacion)
);

ALTER TABLE pagos
ADD COLUMN IF NOT EXISTS restaurante_id INT NULL,
ADD COLUMN IF NOT EXISTS convenio_id INT NULL,
ADD COLUMN IF NOT EXISTS liquidacion_restaurante_id INT NULL,
ADD COLUMN IF NOT EXISTS estado_liquidacion VARCHAR(20) NOT NULL DEFAULT 'NO_APLICA';

ALTER TABLE usuarios
MODIFY COLUMN contrasena VARCHAR(255) NOT NULL;
