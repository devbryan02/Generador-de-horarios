USE [$(DATABASE_NAME)];
GO
-- Datos sinteticos para desarrollo local. No reemplaza ni elimina registros.
SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF NOT EXISTS (SELECT 1 FROM dbo.periodos WHERE id = '10000000-0000-0000-0000-000000000001')
INSERT dbo.periodos (id, nombre, fecha_inicio, fecha_fin)
VALUES ('10000000-0000-0000-0000-000000000001', '2026-II Demo', '2026-07-01', '2026-12-31');

IF NOT EXISTS (SELECT 1 FROM dbo.unidad_academica WHERE id = '00000000-0000-0000-0000-000000000001')
INSERT dbo.unidad_academica (id, nombre)
VALUES ('00000000-0000-0000-0000-000000000001', 'Unidad academica demo');

IF NOT EXISTS (SELECT 1 FROM dbo.aulas WHERE id = '30000000-0000-0000-0000-000000000001')
INSERT dbo.aulas (id, nombre, capacidad, tipo) VALUES
('30000000-0000-0000-0000-000000000001', 'Aula Demo 1', 30, 'TEORICO');
IF NOT EXISTS (SELECT 1 FROM dbo.aulas WHERE id = '30000000-0000-0000-0000-000000000002')
INSERT dbo.aulas (id, nombre, capacidad, tipo) VALUES
('30000000-0000-0000-0000-000000000002', 'Aula Demo 2', 30, 'TEORICO');

IF NOT EXISTS (SELECT 1 FROM dbo.docentes WHERE id = '40000000-0000-0000-0000-000000000001')
INSERT dbo.docentes (id, nombre, horas_contratadas, horas_maximas_por_dia) VALUES
('40000000-0000-0000-0000-000000000001', 'Ana Demo', 12, 6);
IF NOT EXISTS (SELECT 1 FROM dbo.docentes WHERE id = '40000000-0000-0000-0000-000000000002')
INSERT dbo.docentes (id, nombre, horas_contratadas, horas_maximas_por_dia) VALUES
('40000000-0000-0000-0000-000000000002', 'Luis Demo', 12, 6);

IF NOT EXISTS (SELECT 1 FROM dbo.cursos WHERE id = '50000000-0000-0000-0000-000000000001')
INSERT dbo.cursos (id, nombre, horas_semanales, tipo) VALUES
('50000000-0000-0000-0000-000000000001', 'Matematica Demo', 4, 'TEORICO');
IF NOT EXISTS (SELECT 1 FROM dbo.cursos WHERE id = '50000000-0000-0000-0000-000000000002')
INSERT dbo.cursos (id, nombre, horas_semanales, tipo) VALUES
('50000000-0000-0000-0000-000000000002', 'Comunicacion Demo', 4, 'TEORICO');

IF NOT EXISTS (SELECT 1 FROM dbo.secciones WHERE id = '20000000-0000-0000-0000-000000000001')
INSERT dbo.secciones (id, nombre, periodo_id) VALUES
('20000000-0000-0000-0000-000000000001', 'Demo A', '10000000-0000-0000-0000-000000000001');
IF NOT EXISTS (SELECT 1 FROM dbo.secciones WHERE id = '20000000-0000-0000-0000-000000000002')
INSERT dbo.secciones (id, nombre, periodo_id) VALUES
('20000000-0000-0000-0000-000000000002', 'Demo B', '10000000-0000-0000-0000-000000000001');

INSERT dbo.bloque_horario (id, dia_semana, hora_inicio, hora_fin, turno)
SELECT seed.id, seed.dia, seed.inicio, seed.fin, 'MANANA'
FROM (VALUES
    (CAST('70000000-0000-0000-0000-000000000001' AS uniqueidentifier), 'LUNES', '08:00', '10:00'),
    (CAST('70000000-0000-0000-0000-000000000002' AS uniqueidentifier), 'LUNES', '11:00', '13:00'),
    (CAST('70000000-0000-0000-0000-000000000003' AS uniqueidentifier), 'MARTES', '08:00', '10:00'),
    (CAST('70000000-0000-0000-0000-000000000004' AS uniqueidentifier), 'MARTES', '11:00', '13:00'),
    (CAST('70000000-0000-0000-0000-000000000005' AS uniqueidentifier), 'MIERCOLES', '08:00', '10:00'),
    (CAST('70000000-0000-0000-0000-000000000006' AS uniqueidentifier), 'MIERCOLES', '11:00', '13:00')
) seed(id, dia, inicio, fin)
WHERE NOT EXISTS (SELECT 1 FROM dbo.bloque_horario b WHERE b.id = seed.id);

IF NOT EXISTS (SELECT 1 FROM dbo.[curso_seccion-docente] WHERE id = '60000000-0000-0000-0000-000000000001')
INSERT dbo.[curso_seccion-docente] (id, curso_id, seccion_id, docente_id, modo) VALUES
('60000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001',
 '20000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 'PRESENCIAL');
IF NOT EXISTS (SELECT 1 FROM dbo.[curso_seccion-docente] WHERE id = '60000000-0000-0000-0000-000000000002')
INSERT dbo.[curso_seccion-docente] (id, curso_id, seccion_id, docente_id, modo) VALUES
('60000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002',
 '20000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000002', 'PRESENCIAL');

INSERT dbo.curso_unidad_academica (curso_id, unidad_academica_id)
SELECT c.id, '00000000-0000-0000-0000-000000000001'
FROM dbo.cursos c
WHERE c.id IN ('50000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000002')
AND NOT EXISTS (SELECT 1 FROM dbo.curso_unidad_academica cu
    WHERE cu.curso_id = c.id AND cu.unidad_academica_id = '00000000-0000-0000-0000-000000000001');

INSERT dbo.docente_unidad (docente_id, unidad_id)
SELECT d.id, '00000000-0000-0000-0000-000000000001'
FROM dbo.docentes d
WHERE d.id IN ('40000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002')
AND NOT EXISTS (SELECT 1 FROM dbo.docente_unidad du
    WHERE du.docente_id = d.id AND du.unidad_id = '00000000-0000-0000-0000-000000000001');

IF NOT EXISTS (SELECT 1 FROM dbo.restriccion_docente WHERE id = '80000000-0000-0000-0000-000000000001')
INSERT dbo.restriccion_docente (id, dia_semana, hora_inicio, hora_fin, tipo_restriccion, docente_id) VALUES
('80000000-0000-0000-0000-000000000001', 'LUNES', '08:00', '10:00', 'BLOQUEADO',
 '40000000-0000-0000-0000-000000000001');

-- Usuarios sinteticos: coordinador / admin; clave local: LocalDemo2026!
IF NOT EXISTS (SELECT 1 FROM dbo.usuarios WHERE username = 'coordinador')
INSERT dbo.usuarios (username, password, role, enabled)
VALUES ('coordinador', '$2a$10$IJXuPXp1Ebjj5n0ckHqRRuZaImOLjN/Wqp7E7hb9KrVrZfFSAgOva', 'COORDINADOR', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.usuarios WHERE username = 'admin')
INSERT dbo.usuarios (username, password, role, enabled)
VALUES ('admin', '$2a$10$IJXuPXp1Ebjj5n0ckHqRRuZaImOLjN/Wqp7E7hb9KrVrZfFSAgOva', 'ADMIN', 1);

COMMIT TRANSACTION;
GO
