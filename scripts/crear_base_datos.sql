-- ========================================
-- BASE DE DATOS
-- ========================================
DROP DATABASE IF EXISTS estacionamiento;
CREATE DATABASE estacionamiento CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE estacionamiento;

-- ========================================
-- TABLA: estacionamientos
-- ========================================
CREATE TABLE estacionamientos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    total_cajones INT NOT NULL,
    cajones_disponibles INT NOT NULL,
    ciudad VARCHAR(50),
    provincia VARCHAR(50),
    codigo_postal VARCHAR(10),
    INDEX idx_nombre (nombre)
);

-- ========================================
-- TABLA: usuarios
-- ========================================
CREATE TABLE usuarios (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    usuario VARCHAR(50) UNIQUE NOT NULL,
    contrasena VARCHAR(100) NOT NULL,
    rol INT NOT NULL DEFAULT 3,
    estacionamiento_id INT,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (estacionamiento_id) REFERENCES estacionamientos(id),
    INDEX idx_usuario (usuario),
    INDEX idx_rol (rol),
    INDEX idx_estacionamiento (estacionamiento_id)
);

-- ========================================
-- TABLA: cajones
-- ========================================
CREATE TABLE cajones (
    id INT PRIMARY KEY AUTO_INCREMENT,
    numero INT NOT NULL,
    tipo VARCHAR(20) NOT NULL DEFAULT 'Normal',
    estado VARCHAR(20) NOT NULL DEFAULT 'Disponible',
    estacionamiento_id INT NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (estacionamiento_id) REFERENCES estacionamientos(id),
    INDEX idx_estado (estado),
    INDEX idx_estacionamiento (estacionamiento_id),
    UNIQUE KEY unique_cajon (numero, estacionamiento_id)
);

-- ========================================
-- TABLA: clientes
-- ========================================
CREATE TABLE clientes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    telefono VARCHAR(20),
    numero_documento VARCHAR(20) UNIQUE,
    tipo_documento VARCHAR(20),
    ciudad VARCHAR(50),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    INDEX idx_documento (numero_documento),
    INDEX idx_nombre (nombre),
    INDEX idx_activo (activo)
);

-- ========================================
-- TABLA: vehiculos
-- ========================================
CREATE TABLE vehiculos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    patente VARCHAR(10) UNIQUE NOT NULL,
    marca VARCHAR(50),
    modelo VARCHAR(50),
    color VARCHAR(30),
    cliente_id INT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    INDEX idx_patente (patente),
    INDEX idx_cliente (cliente_id),
    INDEX idx_tipo (tipo)
);

-- ========================================
-- TABLA: precios
-- ========================================
CREATE TABLE precios (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tipo_vehiculo VARCHAR(20) NOT NULL,
    precio_hora DECIMAL(10,2),
    precio_media DECIMAL(10,2),
    precio_dia DECIMAL(10,2),
    estacionamiento_id INT NOT NULL,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (estacionamiento_id) REFERENCES estacionamientos(id),
    INDEX idx_tipo_vehiculo (tipo_vehiculo),
    INDEX idx_estacionamiento (estacionamiento_id),
    UNIQUE KEY unique_precio (tipo_vehiculo, estacionamiento_id)
);

-- ========================================
-- TABLA: registros
-- ========================================
CREATE TABLE registros_entrada_salida (
    id INT PRIMARY KEY AUTO_INCREMENT,
    vehiculo_id INT NOT NULL,
    cajon_id INT NOT NULL,
    fecha_entrada TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_salida TIMESTAMP,
    monto DECIMAL(10,2),
    promocion_aplicada VARCHAR(200),
    estado VARCHAR(20) NOT NULL DEFAULT 'Activo',
    estacionamiento_id INT NOT NULL,
    FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id),
    FOREIGN KEY (cajon_id) REFERENCES cajones(id),
    FOREIGN KEY (estacionamiento_id) REFERENCES estacionamientos(id),
    INDEX idx_estado (estado),
    INDEX idx_fecha_entrada (fecha_entrada),
    INDEX idx_vehiculo (vehiculo_id),
    INDEX idx_estacionamiento (estacionamiento_id)
);

-- ========================================
-- TABLA: pensiones
-- ========================================
CREATE TABLE pensiones (
    id INT PRIMARY KEY AUTO_INCREMENT,
    cliente_id INT NOT NULL,
    vehiculo_id INT NOT NULL,
    cajon_id INT NOT NULL,
    fecha_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_fin TIMESTAMP NOT NULL,
    monto DECIMAL(10,2),
    estado VARCHAR(20) NOT NULL DEFAULT 'Activa',
    estacionamiento_id INT NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id),
    FOREIGN KEY (cajon_id) REFERENCES cajones(id),
    FOREIGN KEY (estacionamiento_id) REFERENCES estacionamientos(id),
    INDEX idx_cliente (cliente_id),
    INDEX idx_estado (estado),
    INDEX idx_fecha_inicio (fecha_inicio)
);

-- ========================================
-- TABLA: promociones
-- ========================================
CREATE TABLE promociones (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    descuento_porcentaje DECIMAL(5,2),
    descuento_fijo DECIMAL(10,2) DEFAULT 0,
    horas_gratis INT DEFAULT 0,
    fecha_inicio TIMESTAMP,
    fecha_fin TIMESTAMP,
    tipo_vehiculo VARCHAR(20),
    estacionamiento_id INT NOT NULL,
    activa BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (estacionamiento_id) REFERENCES estacionamientos(id),
    INDEX idx_activa (activa),
    INDEX idx_estacionamiento (estacionamiento_id)
);

-- ========================================
-- TABLA: historial de eventos
-- ========================================
CREATE TABLE historial_eventos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    cliente_id INT NOT NULL,
    vehiculo_id INT NOT NULL,
    registro_id INT,
    cajon_id INT,
    tipo VARCHAR(50) NOT NULL,
    descripcion TEXT,
    monto DECIMAL(10,2) DEFAULT 0,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estacionamiento_id INT NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id),
    FOREIGN KEY (registro_id) REFERENCES registros_entrada_salida(id),
    FOREIGN KEY (cajon_id) REFERENCES cajones(id),
    FOREIGN KEY (estacionamiento_id) REFERENCES estacionamientos(id),
    INDEX idx_cliente (cliente_id),
    INDEX idx_vehiculo (vehiculo_id),
    INDEX idx_registro (registro_id),
    INDEX idx_fecha (fecha)
);

-- ========================================
-- TABLA: restaurantes
-- ========================================
CREATE TABLE restaurantes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    telefono VARCHAR(20),
    email VARCHAR(100),
    comision_porcentaje DECIMAL(5,2),
    estacionamiento_id INT NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (estacionamiento_id) REFERENCES estacionamientos(id),
    INDEX idx_nombre (nombre),
    INDEX idx_activo (activo)
);

-- ========================================
-- TABLA: convenios
-- ========================================
CREATE TABLE convenios_restaurante (
    id INT PRIMARY KEY AUTO_INCREMENT,
    restaurante_id INT NOT NULL,
    descripcion TEXT,
    fecha_inicio TIMESTAMP,
    fecha_fin TIMESTAMP,
    estado VARCHAR(20) NOT NULL DEFAULT 'Vigente',
    estacionamiento_id INT NOT NULL,
    FOREIGN KEY (restaurante_id) REFERENCES restaurantes(id),
    FOREIGN KEY (estacionamiento_id) REFERENCES estacionamientos(id),
    INDEX idx_estado (estado)
);

-- ========================================
-- TABLA: clientes_restaurante
-- ========================================
CREATE TABLE clientes_restaurante (
    id INT PRIMARY KEY AUTO_INCREMENT,
    cliente_id INT NOT NULL,
    restaurante_id INT NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    FOREIGN KEY (restaurante_id) REFERENCES restaurantes(id),
    INDEX idx_cliente (cliente_id),
    INDEX idx_restaurante (restaurante_id)
);

-- ========================================
-- TABLA: uso restaurante
-- ========================================
CREATE TABLE registros_uso_restaurante (
    id INT PRIMARY KEY AUTO_INCREMENT,
    cliente_id INT NOT NULL,
    restaurante_id INT NOT NULL,
    monto DECIMAL(10,2),
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    descripcion TEXT,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    FOREIGN KEY (restaurante_id) REFERENCES restaurantes(id),
    INDEX idx_fecha (fecha),
    INDEX idx_cliente (cliente_id),
    INDEX idx_restaurante (restaurante_id)
);

-- ========================================
-- TABLA: facturas restaurante
-- ========================================
CREATE TABLE facturas_restaurante (
    id INT PRIMARY KEY AUTO_INCREMENT,
    restaurante_id INT NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    monto_total DECIMAL(10,2),
    comision DECIMAL(10,2),
    monto_neto DECIMAL(10,2),
    estado VARCHAR(20) NOT NULL DEFAULT 'Pendiente',
    FOREIGN KEY (restaurante_id) REFERENCES restaurantes(id),
    INDEX idx_fecha (fecha),
    INDEX idx_estado (estado)
);

-- ========================================
-- TABLA: notificaciones
-- ========================================
CREATE TABLE notificaciones (
    id INT PRIMARY KEY AUTO_INCREMENT,
    usuario_id INT NOT NULL,
    titulo VARCHAR(200),
    mensaje TEXT,
    tipo VARCHAR(20) NOT NULL DEFAULT 'Info',
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    leida BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    INDEX idx_usuario (usuario_id),
    INDEX idx_leida (leida)
);

-- ========================================
-- TABLA: configuracion
-- ========================================
CREATE TABLE configuracion (
    id INT PRIMARY KEY AUTO_INCREMENT,
    clave VARCHAR(100) UNIQUE NOT NULL,
    valor TEXT,
    descripcion VARCHAR(200),
    estacionamiento_id INT,
    FOREIGN KEY (estacionamiento_id) REFERENCES estacionamientos(id),
    INDEX idx_clave (clave)
);

-- ========================================
-- DATOS INICIALES COMPLETOS
-- ========================================

INSERT INTO usuarios (nombre, apellido, email, usuario, contrasena, rol)
VALUES ('Administrador', 'Sistema', 'admin@estacionamiento.com', 'admin', 'admin123', 1);

INSERT INTO estacionamientos (nombre, direccion, telefono, email, total_cajones, cajones_disponibles, ciudad, provincia, codigo_postal)
VALUES 
('Estacionamiento Central', 'Calle Principal 123', '555-0001', 'central@estacionamiento.com', 50, 50, 'Ciudad Centro', 'Provincia', '28001'),
('Estacionamiento Norte', 'Avenida Norte 456', '555-0002', 'norte@estacionamiento.com', 75, 75, 'Ciudad Norte', 'Provincia', '28002'),
('Estacionamiento Sur', 'Calle Sur 789', '555-0003', 'sur@estacionamiento.com', 60, 60, 'Ciudad Sur', 'Provincia', '28003');

-- Índices extra
CREATE INDEX idx_registros_fecha_salida ON registros_entrada_salida(fecha_salida);
CREATE INDEX idx_pensiones_fecha_fin ON pensiones(fecha_fin);
CREATE INDEX idx_usuarios_fecha_creacion ON usuarios(fecha_creacion);

SELECT 'Base de datos creada correctamente' AS resultado;