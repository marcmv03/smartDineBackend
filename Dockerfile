# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Copy the JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Note: SSL certificates are mounted via Docker volume from host /etc/letsencrypt
# No need to copy certificates into the image

# Expose ports (8080 for HTTP, 8443 for HTTPS)
EXPOSE 8080 8443

# Set environment variables (can be overridden by docker-compose)
ENV SPRING_PROFILES_ACTIVE=prod

# Health check using HTTPS with curl
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f -k https://localhost:8443/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
