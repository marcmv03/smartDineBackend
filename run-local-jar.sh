#!/bin/bash
# Script to clean, package, test, and run the Spring Boot application with dev profile

echo "========================================="
echo "Step 1: Running clean package with tests"
echo "========================================="

# Run clean package with tests
./mvnw clean package

# Check if the build was successful
if [ $? -ne 0 ]; then
    echo "========================================="
    echo "ERROR: Build or tests failed!"
    echo "========================================="
    exit 1
fi

echo ""
echo "========================================="
echo "Build successful! Starting application..."
echo "========================================="
echo ""

# Load environment variables from .env file
set -a
source .env
set +a

# Run the Spring Boot application with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev