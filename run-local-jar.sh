#!/bin/bash
# Script to run the packaged JAR with environment variables loaded from .env

# Load environment variables from .env file
set -a
source .env
set +a

# Run the Spring Boot JAR
java -jar target/demo-0.0.1-SNAPSHOT.jar
