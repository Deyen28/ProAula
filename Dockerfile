# Usa una imagen base de OpenJDK 21 (coincide con tu pom.xml)
FROM openjdk:21-jdk-slim

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el wrapper de Maven y el pom.xml para descargar dependencias primero.
# Esto aprovecha el cache de capas de Docker si las dependencias no cambian.
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Descarga las dependencias del proyecto
RUN ./mvnw dependency:go-offline -B

# Copia el resto del código fuente de la aplicación
COPY src ./src

# Empaqueta la aplicación (crea el JAR en target/), saltando los tests para un build más rápido
RUN ./mvnw package -DskipTests

# Expone el puerto en el que corre la aplicación Spring Boot (definido en application.properties)
EXPOSE 8081

# Comando para ejecutar la aplicación cuando el contenedor inicie
# Usa el JAR generado en el paso anterior, que estará en target/
ENTRYPOINT ["java", "-jar", "target/ProAula-0.0.1-SNAPSHOT.jar"]