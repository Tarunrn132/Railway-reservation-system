# ===================================================================
# Multi-Stage Production Dockerfile for Railway Reservation System
# Java 21 + Spring Boot 3.3.4 + Embedded Web App & Actuator
# ===================================================================

# --- Stage 1: Build & Package ---
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace/app

# Copy Maven Wrapper & POM
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download dependencies offline for fast layer caching
RUN ./mvnw dependency:go-offline -B

# Copy source code & resources
COPY src ./src

# Build production jar without running test suite in container build
RUN ./mvnw clean package -DskipTests -B

# --- Stage 2: Minimal Production JRE Runtime ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create unprivileged application user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy packaged JAR from builder stage
COPY --from=builder /workspace/app/target/railway-reservation-*.jar app.jar

# Expose dynamic HTTP application port
EXPOSE 8080

# Configure JVM tuning for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Health check using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://127.0.0.1:${PORT:-8080}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
