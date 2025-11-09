# Docker Setup for SmartDine Backend

This guide explains how to run the SmartDine Backend application using Docker Compose.

## Prerequisites

- Docker Desktop installed (version 20.10 or higher)
- Docker Compose (included with Docker Desktop)

## Architecture

The `compose.yaml` file configures two services:

1. **postgres**: PostgreSQL 17.6 database
2. **springboot-app**: Spring Boot application

## Docker Compose Commands

### Start all services

```bash
docker-compose up -d
```

### View logs in real-time

```bash
# All services
docker-compose logs -f

# Spring Boot only
docker-compose logs -f springboot-app

# PostgreSQL only
docker-compose logs -f postgres
```

### Stop services

```bash
docker-compose down
```

### Stop and remove volumes (⚠️ deletes database data)

```bash
docker-compose down -v
```

### Rebuild Spring Boot image

```bash
docker-compose up -d --build springboot-app
```

### Check service status

```bash
docker-compose ps
```

## Service Access

- **REST API (HTTP)**: http://localhost:8080
- **REST API (HTTPS)**: https://localhost:8443 ⭐ **Recommended for production**
- **PostgreSQL**: localhost:5432
  - Database: `smartDine`
  - Username: `postgres`
  - Password: `mV00R152`

### SSL/HTTPS Configuration

The application is configured to use HTTPS with a self-signed certificate:

- **Certificate**: `src/main/resources/smartdine.p12` (PKCS12 format)
- **Certificate password**: `mV00R152`
- **Alias**: `smartdine`
- **Protocol**: TLS 1.2 and TLS 1.3

When accessing via HTTPS, your browser will warn about the self-signed certificate. For production, replace with a valid certificate from a trusted CA.

**Test HTTPS connection**:

```bash
# Using curl (ignoring self-signed certificate)
curl -k https://localhost:8443/actuator/health

# Using wget
wget --no-check-certificate https://localhost:8443/actuator/health
```

## Connect to PostgreSQL from host

```bash
psql -h localhost -p 5432 -U postgres -d smartDine
```

## Execute commands inside containers

### Access Spring Boot container

```bash
docker exec -it smartdine-backend sh
```

### Access PostgreSQL container

```bash
docker exec -it smartdine-postgres psql -U postgres -d smartDine
```

## Troubleshooting

### Check if containers are running

```bash
docker ps
```

### View error logs

```bash
docker-compose logs --tail=100
```

### Restart a specific service

```bash
docker-compose restart springboot-app
```

### Clean everything and start fresh

```bash
docker-compose down -v
docker-compose up -d --build
```

## Environment Variables

Environment variables can be configured in two ways:

1. **Using `.env` file** (recommended):
   - Copy `.env.example` to `.env`
   - Modify values as needed
   - Docker Compose will automatically load these variables

2. **Directly in `compose.yaml`**:
   - Edit the `environment` section of each service

### Key environment variables:

- `APP_PORT`: HTTP port (default: 8080)
- `APP_HTTPS_PORT`: HTTPS port (default: 8443)
- `SPRING_DATASOURCE_URL`: PostgreSQL connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `SPRING_JPA_HIBERNATE_DDL_AUTO`: Schema update mode (update)
- `SECURITY_JWT_SECRET_KEY`: JWT secret key
- `SECURITY_JWT_EXPIRATION_TIME`: Token expiration time in milliseconds
- `SSL_KEY_STORE_PASSWORD`: SSL certificate password

## Data Persistence

PostgreSQL data is persisted in a Docker volume named `postgres-data`. This means data will survive container restarts, but will be deleted if you run `docker-compose down -v`.

## Health Checks

Both services have health checks configured:

- **postgres**: Verifies that PostgreSQL is ready to accept connections
- **springboot-app**: Waits for postgres to be healthy before starting

## Network

Both services are on the `smartdine-network` network, allowing them to communicate using service names (e.g., `postgres:5432`).

## Production Notes

⚠️ **Security checklist for production deployment**:

1. **Replace the self-signed certificate**:
   - Obtain a valid SSL certificate from a trusted CA (Let's Encrypt, DigiCert, etc.)
   - Replace `src/main/resources/smartdine.p12` with your production certificate
   - Update `SSL_KEY_STORE_PASSWORD` environment variable

2. **Change database credentials**:
   - Use strong, unique passwords
   - Store credentials in Docker secrets or environment variables

3. **Secure JWT configuration**:
   - Generate a new, random `SECURITY_JWT_SECRET_KEY`
   - Use at least 256 bits of entropy

4. **Limit port exposure**:
   - Consider removing HTTP port (8080) exposure if using HTTPS only
   - Use a reverse proxy (nginx, traefik) for additional security

5. **Use specific image versions**:
   - Pin PostgreSQL version (e.g., `postgres:17.6-alpine`)
   - Tag your Spring Boot image with version numbers

6. **Configure database backups**:
   - Set up automated PostgreSQL backups
   - Test restore procedures

7. **Additional security measures**:
   - Enable Spring Security CSRF protection if needed
   - Configure CORS properly for your frontend domain
   - Use HTTPS-only cookies for JWT tokens
   - Implement rate limiting
   - Enable Docker security scanning

8. **Monitoring and logging**:
   - Configure centralized logging (ELK, Splunk, etc.)
   - Set up health check monitoring
   - Enable Spring Boot Actuator endpoints securely
