# Las pruebas de SQL Server se ejecutan por separado con el perfil integration.
FROM maven:3.10.0-eclipse-temurin-25 AS build

WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B clean verify

FROM eclipse-temurin:25-jre

WORKDIR /app
COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
