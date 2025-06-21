# Simple Dockerfile for Students - Optimized with lightweight Alpine images
# Jenkins will provide the source code - no git cloning here!

# Stage 1: Build the application
FROM maven:alpine AS build

# Set working directory
WORKDIR /app

# Copy source code (provided by Jenkins)
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -Djacoco.skip=true

# Stage 2: Run the application
FROM maven:alpine

# Install curl for health checks (using Alpine package manager)
RUN apk add --no-cache curl

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 