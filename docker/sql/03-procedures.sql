USE [$(DATABASE_NAME)];
GO
-- Contratos locales de desarrollo, no una recuperacion de los procedimientos
-- originales. Aulas y bloques son catalogos compartidos sin filtro adicional.
CREATE OR ALTER PROCEDURE dbo.sp_aulas_disponibles_entidad
    @periodoId uniqueidentifier
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT EXISTS (SELECT 1 FROM dbo.periodos WHERE id = @periodoId)
        THROW 50001, 'Periodo no encontrado', 1;
    SELECT id, nombre, capacidad, tipo FROM dbo.aulas ORDER BY nombre;
END;
GO
CREATE OR ALTER PROCEDURE dbo.sp_bloques_disponibles_entidad
    @periodoId uniqueidentifier
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT EXISTS (SELECT 1 FROM dbo.periodos WHERE id = @periodoId)
        THROW 50001, 'Periodo no encontrado', 1;
    SELECT id, dia_semana, hora_inicio, hora_fin, turno
    FROM dbo.bloque_horario ORDER BY dia_semana, hora_inicio;
END;
GO
CREATE OR ALTER PROCEDURE dbo.sp_curso_seccion_docente_por_periodo
    @periodoId uniqueidentifier
AS
BEGIN
    SET NOCOUNT ON;
    SELECT relacion.id, relacion.curso_id, relacion.seccion_id,
           relacion.docente_id, relacion.modo
    FROM dbo.[curso_seccion-docente] relacion
    INNER JOIN dbo.secciones seccion ON seccion.id = relacion.seccion_id
    WHERE seccion.periodo_id = @periodoId
    ORDER BY relacion.id;
END;
GO
