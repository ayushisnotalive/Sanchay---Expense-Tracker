# Multi-stage Dockerfile for Sanchay Enterprise Spring Boot Backend
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy Gradle files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY backend backend

# Build runnable Spring Boot jar
RUN chmod +x ./gradlew && ./gradlew :backend:bootJar -x test --no-daemon

# Production Runtime Container
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S sanchay && adduser -S sanchay -G sanchay
USER sanchay:sanchay

COPY --from=builder /workspace/backend/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:+ZGenerational", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
