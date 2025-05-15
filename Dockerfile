# Etapa 2: Crear la imagen final a partir de una imagen base de Java
FROM openjdk:21-jdk-slim

# Establecer el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar el archivo JAR que ya compilaste localmente
COPY target/ProAula-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

# Comando para ejecutar la aplicación cuando el contenedor inicie
ENTRYPOINT ["java", "-jar", "app.jar"]