#!/bin/bash

# SmartDine Backend - Azure Deployment Script
# This script automates the deployment process on Azure VM

set -e  # Exit on any error

echo "======================================"
echo "SmartDine Backend - Azure Deployment"
echo "======================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print colored messages
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Check if running as root
if [ "$EUID" -eq 0 ]; then 
    print_error "Please do not run this script as root"
    exit 1
fi

# Step 1: Check prerequisites
echo "Step 1: Checking prerequisites..."

if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi
print_success "Docker is installed"

# Check for docker compose (new) or docker-compose (old)
if docker compose version &> /dev/null; then
    print_success "Docker Compose is installed (docker compose)"
    DOCKER_COMPOSE="docker compose"
elif command -v docker-compose &> /dev/null; then
    print_success "Docker Compose is installed (docker-compose)"
    DOCKER_COMPOSE="docker-compose"
else
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Step 2: Check if .env file exists
echo ""
echo "Step 2: Checking environment configuration..."

if [ ! -f ".env" ]; then
    print_warning ".env file not found. Creating from .env.example..."
    if [ -f ".env.example" ]; then
        cp .env.example .env
        print_success "Created .env file from template"
        print_warning "Please edit .env file with your production values before continuing!"
        read -p "Press Enter to edit .env file now..." 
        ${EDITOR:-nano} .env
    else
        print_error ".env.example file not found!"
        exit 1
    fi
else
    print_success ".env file exists"
fi

# Step 3: Validate critical environment variables
echo ""
echo "Step 3: Validating environment variables..."

source .env

if [ "$POSTGRES_PASSWORD" == "mV00R152" ]; then
    print_warning "Using default PostgreSQL password. Consider changing it for production!"
fi

if [ "$SECURITY_JWT_SECRET_KEY" == "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970" ]; then
    print_warning "Using default JWT secret key. Consider changing it for production!"
fi

print_success "Environment variables loaded"

# Step 4: Check SSL certificate
echo ""
echo "Step 4: Checking SSL certificate..."

if [ -f "src/main/resources/smartdine.p12" ]; then
    print_success "SSL certificate found"
else
    print_error "SSL certificate not found at src/main/resources/smartdine.p12"
    print_warning "Please add your SSL certificate or the application will fail to start"
    exit 1
fi

# Step 5: Stop existing containers
echo ""
echo "Step 5: Stopping existing containers (if any)..."

if $DOCKER_COMPOSE ps | grep -q "Up"; then
    $DOCKER_COMPOSE down
    print_success "Stopped existing containers"
else
    print_success "No running containers found"
fi

# Step 6: Clean up old images (optional)
echo ""
read -p "Do you want to clean up old Docker images? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    docker image prune -f
    print_success "Cleaned up old images"
fi

# Step 7: Build and start containers
echo ""
echo "Step 7: Building and starting containers..."
echo "This may take several minutes..."

if $DOCKER_COMPOSE up -d --build; then
    print_success "Containers started successfully"
else
    print_error "Failed to start containers"
    exit 1
fi

# Step 8: Wait for services to be healthy
echo ""
echo "Step 8: Waiting for services to be healthy..."

MAX_ATTEMPTS=30
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    POSTGRES_HEALTH=$(docker inspect --format='{{.State.Health.Status}}' smartdine-postgres 2>/dev/null || echo "starting")
    BACKEND_HEALTH=$(docker inspect --format='{{.State.Health.Status}}' smartdine-backend 2>/dev/null || echo "starting")
    
    if [ "$POSTGRES_HEALTH" == "healthy" ] && [ "$BACKEND_HEALTH" == "healthy" ]; then
        print_success "All services are healthy!"
        break
    fi
    
    echo -n "."
    sleep 5
    ATTEMPT=$((ATTEMPT+1))
done

if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
    print_warning "Services are taking longer than expected to become healthy"
    print_warning "Check logs with: $DOCKER_COMPOSE logs -f"
fi

# Step 9: Display service status
echo ""
echo "Step 9: Service Status"
echo "======================"
$DOCKER_COMPOSE ps

# Step 10: Test endpoints
echo ""
echo "Step 10: Testing endpoints..."

echo "Testing HTTPS health endpoint..."
if curl -k -f https://localhost:8443/actuator/health &> /dev/null; then
    print_success "HTTPS endpoint is responding"
else
    print_warning "HTTPS endpoint not responding yet (may still be starting)"
fi

# Step 11: Display access information
echo ""
echo "======================================"
echo "Deployment Complete!"
echo "======================================"
echo ""
echo "Access Information:"
echo "-------------------"
echo "HTTPS API: https://$(curl -s ifconfig.me):8443"
echo "HTTP API:  http://$(curl -s ifconfig.me):8080 (if enabled)"
echo ""
echo "Health Check: https://$(curl -s ifconfig.me):8443/actuator/health"
echo ""
echo "Useful Commands:"
echo "----------------"
echo "View logs:        $DOCKER_COMPOSE logs -f"
echo "Stop services:    $DOCKER_COMPOSE down"
echo "Restart services: $DOCKER_COMPOSE restart"
echo "Check status:     $DOCKER_COMPOSE ps"
echo ""
echo "Next Steps:"
echo "-----------"
echo "1. Ensure Azure NSG allows ports 8080 and 8443"
echo "2. Test the health endpoint from outside the VM"
echo "3. Replace self-signed SSL certificate for production"
echo "4. Change default passwords in .env file"
echo "5. Review logs for any warnings: $DOCKER_COMPOSE logs"
echo ""
print_success "Deployment completed successfully!"
