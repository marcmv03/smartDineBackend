#!/bin/bash

# SmartDine Backend - Monitoring Script
# Run this script to check the health and status of your deployment

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

print_header() {
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Detect docker compose command
if docker compose version &> /dev/null; then
    DOCKER_COMPOSE="docker compose"
elif command -v docker-compose &> /dev/null; then
    DOCKER_COMPOSE="docker-compose"
else
    echo "Error: Docker Compose not found"
    exit 1
fi

# Main monitoring
clear
print_header "SmartDine Backend - System Monitor"

# 1. Container Status
print_header "1. Container Status"
$DOCKER_COMPOSE ps

# 2. Container Health
print_header "2. Container Health"

POSTGRES_HEALTH=$(docker inspect --format='{{.State.Health.Status}}' smartdine-postgres 2>/dev/null || echo "not running")
BACKEND_HEALTH=$(docker inspect --format='{{.State.Health.Status}}' smartdine-backend 2>/dev/null || echo "not running")

if [ "$POSTGRES_HEALTH" == "healthy" ]; then
    print_success "PostgreSQL: $POSTGRES_HEALTH"
else
    print_error "PostgreSQL: $POSTGRES_HEALTH"
fi

if [ "$BACKEND_HEALTH" == "healthy" ]; then
    print_success "Backend: $BACKEND_HEALTH"
else
    print_error "Backend: $BACKEND_HEALTH"
fi

# 3. Resource Usage
print_header "3. Resource Usage"
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}"

# 4. Network Connectivity
print_header "4. Network Connectivity"

# Test internal health endpoint
if curl -k -f https://localhost:8443/actuator/health &> /dev/null; then
    print_success "Internal HTTPS endpoint: OK"
else
    print_error "Internal HTTPS endpoint: FAILED"
fi

# Get public IP
PUBLIC_IP=$(curl -s ifconfig.me)
echo "Public IP: $PUBLIC_IP"

# 5. Recent Logs
print_header "5. Recent Application Logs (Last 20 lines)"
$DOCKER_COMPOSE logs --tail=20 springboot-app

# 6. Recent PostgreSQL Logs
print_header "6. Recent PostgreSQL Logs (Last 10 lines)"
$DOCKER_COMPOSE logs --tail=10 postgres

# 7. Disk Usage
print_header "7. Disk Usage"
docker system df

# 8. Database Info
print_header "8. Database Information"
docker exec smartdine-postgres psql -U postgres -d smartDine -c "\dt" 2>/dev/null || print_error "Cannot connect to database"

# 9. Active Connections
print_header "9. Active Database Connections"
docker exec smartdine-postgres psql -U postgres -d smartDine -c "SELECT count(*) as active_connections FROM pg_stat_activity;" 2>/dev/null || print_error "Cannot query database"

# 10. Summary and Recommendations
print_header "10. Summary"

ISSUES=0

if [ "$POSTGRES_HEALTH" != "healthy" ]; then
    print_warning "PostgreSQL container is not healthy"
    ((ISSUES++))
fi

if [ "$BACKEND_HEALTH" != "healthy" ]; then
    print_warning "Backend container is not healthy"
    ((ISSUES++))
fi

if ! curl -k -f https://localhost:8443/actuator/health &> /dev/null; then
    print_warning "Backend API is not responding"
    ((ISSUES++))
fi

if [ $ISSUES -eq 0 ]; then
    print_success "All systems operational!"
else
    print_warning "Found $ISSUES issue(s) - check logs with: $DOCKER_COMPOSE logs -f"
fi

print_header "Useful Commands"
echo "View real-time logs:     $DOCKER_COMPOSE logs -f"
echo "Restart application:     $DOCKER_COMPOSE restart springboot-app"
echo "Restart PostgreSQL:      $DOCKER_COMPOSE restart postgres"
echo "Stop all services:       $DOCKER_COMPOSE down"
echo "Rebuild and restart:     $DOCKER_COMPOSE up -d --build"
echo ""
