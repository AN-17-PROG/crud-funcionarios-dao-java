-- Actividad: CRUD de funcionarios (patrón DAO)
-- Script de poblado de las tablas

USE recursos_humanos;

-- Catálogos
INSERT IGNORE INTO tipo_documento (id_tipo_documento, nombre) VALUES
(1, 'Cédula de ciudadanía'),
(2, 'Tarjeta de identidad'),
(3, 'Cédula de extranjería'),
(4, 'Pasaporte');

INSERT IGNORE INTO estado_civil (id_estado_civil, nombre) VALUES
(1, 'Soltero'),
(2, 'Casado'),
(3, 'Unión libre'),
(4, 'Divorciado'),
(5, 'Viudo');

-- Funcionarios
INSERT IGNORE INTO funcionarios
(id_funcionario, id_tipo_documento, numero_identificacion, nombres, apellidos, id_estado_civil, sexo, direccion, telefono, fecha_nacimiento)
VALUES
(1, 1, '1020304050', 'Carlos Andrés', 'Pérez Gómez', 2, 'Masculino', 'Calle 10 # 20-30, Medellín', '3001234567', '1985-03-15'),
(2, 1, '1098765432', 'Ana María', 'Rojas Torres', 1, 'Femenino', 'Carrera 45 # 12-08, Medellín', '3109876543', '1992-07-22'),
(3, 3, '5544332211', 'Luis Fernando', 'Martínez Díaz', 3, 'Masculino', 'Avenida 80 # 33-15, Bello', '3205557788', '1979-11-02');

-- Grupo familiar
INSERT IGNORE INTO grupo_familiar
(id_grupo_familiar, id_funcionario, nombres, apellidos, fecha_nacimiento, rol)
VALUES
(1, 1, 'Laura', 'Gómez Ríos', '1987-06-10', 'Esposa'),
(2, 1, 'Sofía', 'Pérez Gómez', '2014-02-18', 'Hija'),
(3, 1, 'Mateo', 'Pérez Gómez', '2017-09-05', 'Hijo'),
(4, 2, 'Marta', 'Torres Vélez', '1962-12-01', 'Madre'),
(5, 3, 'Elena', 'Cardona Ruiz', '1981-04-27', 'Compañera');

-- Formación académica
INSERT IGNORE INTO formacion_academica
(id_formacion, id_funcionario, universidad, nivel_estudio, titulo)
VALUES
(1, 1, 'Universidad de Antioquia', 'Pregrado', 'Ingeniero de Sistemas'),
(2, 1, 'Universidad Nacional de Colombia', 'Maestría', 'Magíster en Gestión de Proyectos'),
(3, 2, 'Universidad EAFIT', 'Pregrado', 'Administradora de Empresas'),
(4, 3, 'SENA', 'Tecnología', 'Tecnólogo en Gestión del Talento Humano');
