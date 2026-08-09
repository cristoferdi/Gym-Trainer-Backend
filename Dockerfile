# Etapa 1: Construcción (Build)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copiamos el archivo pom y el código fuente
COPY pom.xml .
COPY src ./src
# Compilamos ignorando los tests para un despliegue más rápido
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Run)
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
# Copiamos el .jar generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar
# Exponemos el puerto
EXPOSE 3000
# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]