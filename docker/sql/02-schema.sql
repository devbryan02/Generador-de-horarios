USE [$(DATABASE_NAME)];
GO
SET XACT_ABORT ON;
GO

IF OBJECT_ID(N'dbo.periodos', N'U') IS NULL
CREATE TABLE dbo.periodos (
    id uniqueidentifier NOT NULL PRIMARY KEY,
    nombre varchar(255), fecha_inicio date, fecha_fin date
);
GO
IF OBJECT_ID(N'dbo.unidad_academica', N'U') IS NULL
CREATE TABLE dbo.unidad_academica (
    id uniqueidentifier NOT NULL PRIMARY KEY, nombre varchar(255)
);
GO
IF OBJECT_ID(N'dbo.aulas', N'U') IS NULL
CREATE TABLE dbo.aulas (
    id uniqueidentifier NOT NULL PRIMARY KEY,
    nombre varchar(255), capacidad int, tipo varchar(255)
);
GO
IF OBJECT_ID(N'dbo.docentes', N'U') IS NULL
CREATE TABLE dbo.docentes (
    id uniqueidentifier NOT NULL PRIMARY KEY,
    nombre varchar(255), horas_contratadas int, horas_maximas_por_dia int
);
GO
IF OBJECT_ID(N'dbo.cursos', N'U') IS NULL
CREATE TABLE dbo.cursos (
    id uniqueidentifier NOT NULL PRIMARY KEY,
    nombre varchar(255), horas_semanales int, tipo varchar(255)
);
GO
IF OBJECT_ID(N'dbo.bloque_horario', N'U') IS NULL
CREATE TABLE dbo.bloque_horario (
    id uniqueidentifier NOT NULL PRIMARY KEY,
    dia_semana varchar(255), hora_inicio time(7), hora_fin time(7), turno varchar(255)
);
GO
IF OBJECT_ID(N'dbo.secciones', N'U') IS NULL
CREATE TABLE dbo.secciones (
    id uniqueidentifier NOT NULL PRIMARY KEY, nombre varchar(255),
    periodo_id uniqueidentifier NOT NULL REFERENCES dbo.periodos(id)
);
GO
IF OBJECT_ID(N'dbo.curso_unidad_academica', N'U') IS NULL
CREATE TABLE dbo.curso_unidad_academica (
    curso_id uniqueidentifier NOT NULL REFERENCES dbo.cursos(id),
    unidad_academica_id uniqueidentifier NOT NULL REFERENCES dbo.unidad_academica(id),
    PRIMARY KEY (curso_id, unidad_academica_id)
);
GO
IF OBJECT_ID(N'dbo.docente_unidad', N'U') IS NULL
CREATE TABLE dbo.docente_unidad (
    docente_id uniqueidentifier NOT NULL REFERENCES dbo.docentes(id),
    unidad_id uniqueidentifier NOT NULL REFERENCES dbo.unidad_academica(id),
    PRIMARY KEY (docente_id, unidad_id)
);
GO
IF OBJECT_ID(N'dbo.[curso_seccion-docente]', N'U') IS NULL
CREATE TABLE dbo.[curso_seccion-docente] (
    id uniqueidentifier NOT NULL PRIMARY KEY,
    curso_id uniqueidentifier NOT NULL REFERENCES dbo.cursos(id),
    seccion_id uniqueidentifier NOT NULL REFERENCES dbo.secciones(id),
    docente_id uniqueidentifier NOT NULL REFERENCES dbo.docentes(id),
    modo varchar(255)
);
GO
IF OBJECT_ID(N'dbo.restriccion_docente', N'U') IS NULL
CREATE TABLE dbo.restriccion_docente (
    id uniqueidentifier NOT NULL PRIMARY KEY,
    dia_semana varchar(255), hora_inicio time(7), hora_fin time(7),
    tipo_restriccion varchar(255),
    docente_id uniqueidentifier NOT NULL REFERENCES dbo.docentes(id)
);
GO
IF OBJECT_ID(N'dbo.asignacion_horario', N'U') IS NULL
CREATE TABLE dbo.asignacion_horario (
    id uniqueidentifier NOT NULL PRIMARY KEY,
    version bigint NOT NULL DEFAULT 1,
    curso_seccion_id uniqueidentifier NOT NULL REFERENCES dbo.[curso_seccion-docente](id),
    aula_id uniqueidentifier REFERENCES dbo.aulas(id),
    bloque_horario_id uniqueidentifier REFERENCES dbo.bloque_horario(id)
);
GO
IF OBJECT_ID(N'dbo.usuarios', N'U') IS NULL
CREATE TABLE dbo.usuarios (
    id bigint IDENTITY(1,1) NOT NULL PRIMARY KEY,
    username varchar(255) NOT NULL UNIQUE,
    password varchar(255) NOT NULL,
    role varchar(255) NOT NULL,
    enabled bit NOT NULL DEFAULT 1
);
GO
