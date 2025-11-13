# Multi-stage Dockerfile for Spring Boot application

# Stage 1: Build stage
FROM harbor-beijing.easemob.com/yunwei/jdk17:latest AS builder

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .

# Copy version catalog
COPY gradle/libs.versions.toml gradle/

# Copy all module sources
COPY module module
COPY server server

# Fix line endings and make gradlew executable, then build the application
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew build -x test -x spotlessCheck --no-daemon

# Stage 2: Runtime stage
FROM harbor-beijing.easemob.com/yunwei/jdk17:latest

WORKDIR /app

# Create a non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring


# Copy the built JAR from builder stage
COPY --from=builder /app/server/api/build/libs/realworld.jar app.jar

# Change ownership to non-root user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose the application port (default Spring Boot port)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

# Optional: JVM options can be passed as environment variables
# ENV JAVA_OPTS=""
# ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
