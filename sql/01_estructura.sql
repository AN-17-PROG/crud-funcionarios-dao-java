-- Actividad: CRUD de funcionarios (patrón DAO)
-- Script de estructura de la base de datos

CREATE DATABASE IF NOT EXISTS recursos_humanos;
USE recursos_humanos;

-- Catálogos (no dependen de nadie)
CREATE TABLE IF NOT EXISTS tipo_documento (
    id_tipo_documento INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS estado_civil (
    id_estado_civil INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL
);

-- Tabla central
CREATE TABLE IF NOT EXISTS funcionarios (
    id_funcionario INT AUTO_INCREMENT PRIMARY KEY,
    id_tipo_documento INT NOT NULL,
    numero_identificacion VARCHAR(20) NOT NULL UNIQUE,
    nombres VARCHAR(60) NOT NULL,
    apellidos VARCHAR(60) NOT NULL,
    id_estado_civil INT NOT NULL,
    sexo VARCHAR(10) NOT NULL,
    direccion VARCHAR(100),
    telefono VARCHAR(15),
    fecha_nacimiento DATE NOT NULL,
    FOREIGN KEY (id_tipo_documento) REFERENCES tipo_documento(id_tipo_documento),
    FOREIGN KEY (id_estado_civil) REFERENCES estado_civil(id_estado_civil)
);

-- Tablas que dependen de funcionarios
CREATE TABLE IF NOT EXISTS grupo_familiar (
    id_grupo_familiar INT AUTO_INCREMENT PRIMARY KEY,
    id_funcionario INT NOT NULL,
    nombres VARCHAR(60) NOT NULL,
    apellidos VARCHAR(60) NOT NULL,
    fecha_nacimiento DATE,
    rol VARCHAR(30) NOT NULL,
    FOREIGN KEY (id_funcionario) REFERENCES funcionarios(id_funcionario)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS formacion_academica (
    id_formacion INT AUTO_INCREMENT PRIMARY KEY,
    id_funcionario INT NOT NULL,
    universidad VARCHAR(100) NOT NULL,
    nivel_estudio VARCHAR(50) NOT NULL,
    titulo VARCHAR(100) NOT NULL,
    FOREIGN KEY (id_funcionario) REFERENCES funcionarios(id_funcionario)
        ON DELETE CASCADE
);
