-- Base de desarrollo reconstruida desde las entidades JPA; no es el SQL original.
-- DATABASE_NAME es validado por db-init antes de la sustitucion sqlcmd.
IF DB_ID(N'$(DATABASE_NAME)') IS NULL
BEGIN
    EXEC(N'CREATE DATABASE [$(DATABASE_NAME)]');
END;
GO
