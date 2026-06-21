# Usar una imagen base de JDK 17 con Gradle para compilar
FROM gradle:8.7-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

# Compilar solo el módulo backend y generar el archivo JAR ejecutable
RUN ./gradlew :backend:bootJar --no-daemon -x test

# Imagen base ligera de JDK 17 para la ejecución
FROM openjdk:17-jdk-slim
EXPOSE 8080

# Copiar el JAR generado desde la fase de compilación
COPY --from=build /home/gradle/src/backend/build/libs/*.jar app.jar

# Configurar directorio de subida de fotos
RUN mkdir -p /uploads

# Arrancar la aplicación
ENTRYPOINT ["java", "-jar", "/app.jar"]
