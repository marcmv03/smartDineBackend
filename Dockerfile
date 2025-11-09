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

# Copy the JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Copy SSL certificate from resources
COPY src/main/resources/smartdine.p12 /app/keystore/smartdine.p12

# Expose ports (8080 for HTTP, 8443 for HTTPS)
EXPOSE 8080 8443

# Set environment variables (can be overridden by docker-compose)
ENV SPRING_PROFILES_ACTIVE=prod

# Health check using HTTPS
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider --no-check-certificate https://localhost:8443/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
