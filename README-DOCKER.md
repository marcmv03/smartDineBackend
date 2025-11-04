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

- **REST API**: http://localhost:8080
- **PostgreSQL**: localhost:5432
  - Database: `smartDine`
  - Username: `postgres`
  - Password: `mV00R152`

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

Environment variables are configured in `compose.yaml`:

- `SPRING_DATASOURCE_URL`: PostgreSQL connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `SPRING_JPA_HIBERNATE_DDL_AUTO`: Schema update mode (update)
- `SECURITY_JWT_SECRET_KEY`: JWT secret key
- `SECURITY_JWT_EXPIRATION_TIME`: Token expiration time in milliseconds

## Data Persistence

PostgreSQL data is persisted in a Docker volume named `postgres-data`. This means data will survive container restarts, but will be deleted if you run `docker-compose down -v`.

## Health Checks

Both services have health checks configured:

- **postgres**: Verifies that PostgreSQL is ready to accept connections
- **springboot-app**: Waits for postgres to be healthy before starting

## Network

Both services are on the `smartdine-network` network, allowing them to communicate using service names (e.g., `postgres:5432`).

## Production Notes

⚠️ **Do not use this configuration in production without changes**:

1. Change database credentials
2. Use Docker secrets for passwords
3. Configure HTTPS/SSL
4. Limit port exposure
5. Use specific image versions (not `latest`)
6. Configure automatic database backups
