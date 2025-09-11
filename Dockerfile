# ---------- Build Stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app


# Copy everything and build the JAR
COPY . .
RUN mvn clean package -DskipTests



# ---------- Runtime Stage ----------
FROM openjdk:21-jdk-slim
WORKDIR /app


# Copy the built JAR from build stage
COPY --from=build /app/target/URL_Shortener-0.0.1-SNAPSHOT.jar urlshortener.jar


# Expose app port
EXPOSE 8080


# Run the Spring Boot app
ENTRYPOINT ["java","-jar","urlshortener.jar"]
