# Generador de horarios

API de horarios académicos con Java 25 LTS, Spring Boot 4.1.2,
Timefold Solver 2.8.0 y SQL Server. Timefold sustituye a OptaPlanner;
se conservan las reglas académicas y los contratos HTTP existentes.

## Requisitos y configuración

- JDK 25. Maven Wrapper descarga Maven 3.10.0.
- SQL Server con esquema y procedimientos almacenados del proyecto.
- Variables de entorno `DATABASE_DRIVER`, `DATABASE_URL`, `DATABASE_USERNAME`,
  `DATABASE_PASSWORD`, `JWT_SECRET` y `JWT_EXPIRATION`.
- Para el frontend actual: `SERVER_SERVLET_CONTEXT_PATH=/api/v1` y puerto 8080.
- Con el esquema demo fuera de Compose, activar
  `SPRING_JPA_PROPERTIES_HIBERNATE_GLOBALLY_QUOTED_IDENTIFIERS=true` por el nombre
  existente de la tabla `curso_seccion-docente`.

El datasource personalizado consume `database.*`, no `spring.datasource.*`.
Spring resuelve estas propiedades desde las variables de entorno indicadas
o desde un `application.properties` externo. No versionar secretos.
`JWT_SECRET` debe tener al menos 32 bytes para HS256; `JWT_EXPIRATION` usa
milisegundos. No se generan credenciales ni usuarios automáticamente.

## Comandos (PowerShell)

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

La verificación predeterminada ejecuta pruebas sintéticas, de JWT, HTTP,
autorización y configuración Spring/Timefold sin SQL Server. Las pruebas
marcadas `integration` y el benchmark quedan fuera de esa ejecución.

```powershell
# Con SQL Server y configuración disponibles:
.\mvnw.cmd -Pintegration verify
# Benchmark: calentamiento y tres semillas de 30 segundos.
.\mvnw.cmd '-Dtest=HorarioMigrationTest#compareSolverOnSyntheticData' '-DexcludedGroups=integration' test
```

## Arquitectura y generación

- `application/`: casos de uso, DTO y mappers HTTP.
- `domain/`: entidades, contratos de repositorios y restricciones del solver.
- `infrastructure/`: controladores, entidades JPA y adaptadores de persistencia.
- `shared/`: seguridad, configuración y manejo de errores.

La relación curso–sección–docente se configura antes de optimizar.
`POST /horario/resolver?periodoId=<UUID>` prepara el problema, espera al solver
y persiste las asignaciones. Las variables de planificación son aula y bloque.
La configuración productiva está en `TimefoldConfig`: construcción inicial,
búsqueda tabú, late acceptance y límite global de dos minutos, con terminación
anticipada por factibilidad o falta de mejora.

`GET /horarios/seccion/{id}`, `/horarios/docente/{id}` y
`/horarios/periodo/{id}` consultan el resultado. Las rutas son relativas al
context path configurado.

## Docker y límites de la migración

El Dockerfile compila con Java 25, ejecuta las pruebas sin SQL y empaqueta el
JAR. El Compose del workspace está un nivel arriba; incluye SQL Server,
inicialización de la base, backend y frontend. `docker/sql/` contiene un esquema
reconstruido desde JPA, tres procedimientos locales y datos demo idempotentes.
No son los scripts originales. El arranque y las credenciales de desarrollo
están documentados en `../docs/docker-local.md`.

La migración conserva fallos funcionales conocidos, incluidos el borrado
global al regenerar y el guardado de soluciones inviables. No ejecutar la
generación sobre datos reales hasta resolverlos. El informe de migración y
la comparación sintética están en `../docs/` del workspace; esa carpeta no
forma parte de este repositorio Git independiente.
