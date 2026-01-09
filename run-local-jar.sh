#!/bin/bash
# Script to clean, package, test, and run the Spring Boot JAR with environment variables

echo "========================================="
echo "Step 1: Running clean package (skipping tests)"
echo "========================================="

# Run clean package skipping tests
./mvnw clean package -DskipTests

# Check if the build was successful
if [ $? -ne 0 ]; then
    echo "========================================="
    echo "ERROR: Build failed!"
    echo "========================================="
    exit 1
fi

echo ""
echo "========================================="
echo "Build successful! Loading environment..."
echo "========================================="
echo ""

# Load environment variables from .env file
if [ -f .env ]; then
    echo "Loading environment variables from .env..."
    # Read each line and export it
    while IFS='=' read -r key value; do
        # Skip comments and empty lines
        if [[ ! "$key" =~ ^#.* ]] && [[ -n "$key" ]]; then
            # Remove quotes from value if present
            value=$(echo "$value" | sed -e 's/^"//' -e 's/"$//' -e "s/^'//" -e "s/'$//")
            export "$key=$value"
            echo "Loaded: $key"
        fi
    done < .env
else
    echo "WARNING: .env file not found!"
fi

# Set defaults if not loaded from .env
export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/smartDine}"
export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-postgres}"
export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-mV00R152}"
export SECURITY_JWT_SECRET_KEY="${SECURITY_JWT_SECRET_KEY:-404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}"

echo ""
echo "========================================="
echo "Starting application with JAR..."
echo "========================================="
echo "Database URL: $SPRING_DATASOURCE_URL"
echo "Database User: $SPRING_DATASOURCE_USERNAME"
echo ""

# Find and run the JAR file
JAR_FILE=$(find target -name "*.jar" | grep -v "original" | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "ERROR: No JAR file found in target directory!"
    exit 1
fi

echo "Running: $JAR_FILE"
java -jar "$JAR_FILE"