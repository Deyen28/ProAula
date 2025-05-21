# Usa una imagen base de OpenJDK 21
FROM openjdk:21-jdk-slim

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el wrapper de Maven y el pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Otorga permisos de ejecución al Maven Wrapper
RUN chmod +x ./mvnw

# Descarga las dependencias del proyecto
RUN ./mvnw dependency:go-offline -B

# Copia el resto del código fuente de la aplicación
COPY src ./src

# Empaqueta la aplicación (crea el JAR en target/)
RUN ./mvnw package -DskipTests

# Expone el puerto en el que corre la aplicación
EXPOSE 8081

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]