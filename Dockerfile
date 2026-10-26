# Multi-stage build for optimized image size
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
# Téléchargement des dépendances dans une couche séparée (mise en cache entre deux builds)
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Exécution avec un utilisateur non root
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
