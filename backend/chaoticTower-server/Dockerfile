# Stage 1: Build stage (Force rebuild to bust cache)
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
# Add execute permission to gradlew script
RUN chmod +x gradlew
# Running compile without daemon
RUN ./gradlew bootJar --no-daemon

# Stage 2: Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
