# Azure Deployment Guide for SmartDine Backend

## 📋 Pre-Deployment Checklist

### 1. Azure VM Requirements
- [ ] VM Size: At least **Standard B2s** (2 vCPUs, 4 GB RAM) or higher
- [ ] OS: Ubuntu 20.04 LTS or later
- [ ] Open ports in Network Security Group:
  - [ ] Port **22** (SSH)
  - [ ] Port **8080** (HTTP - optional)
  - [ ] Port **8443** (HTTPS - recommended)
  - [ ] Port **5432** (PostgreSQL - only if external access needed)

### 2. Install Docker and Docker Compose on Azure VM

```bash
# Update system
sudo apt-get update
sudo apt-get upgrade -y

# Install Docker
sudo apt-get install -y apt-transport-https ca-certificates curl software-properties-common
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Add current user to docker group (logout and login required)
sudo usermod -aG docker $USER

# Verify installations
docker --version
docker-compose --version
```

### 3. Transfer Files to Azure VM

```bash
# Option 1: Using SCP from local machine
scp -r ./smartDineBackend azureuser@<VM_PUBLIC_IP>:/home/azureuser/

# Option 2: Using Git (recommended)
ssh azureuser@<VM_PUBLIC_IP>
git clone https://github.com/marcmv03/smartDineBackend.git
cd smartDineBackend
```

### 4. Environment Configuration

```bash
# Create .env file from example
cp .env.example .env

# Edit .env with production values
nano .env
```

**⚠️ Important: Change these values in production:**
```env
# PostgreSQL Configuration - CHANGE THESE!
POSTGRES_PASSWORD=<strong-random-password>
SPRING_DATASOURCE_PASSWORD=<strong-random-password>

# Security Configuration - CHANGE THESE!
SECURITY_JWT_SECRET_KEY=<generate-new-256-bit-key>
SSL_KEY_STORE_PASSWORD=<your-certificate-password>
```

Generate a secure JWT secret key:
```bash
# Generate a random 256-bit key
openssl rand -hex 32
```

### 5. SSL Certificate for Production

#### Option A: Use Let's Encrypt (Recommended for production)

```bash
# Install certbot
sudo apt-get install -y certbot

# Generate certificate (replace with your domain)
sudo certbot certonly --standalone -d yourdomain.com

# Convert to PKCS12 format
sudo openssl pkcs12 -export \
  -in /etc/letsencrypt/live/yourdomain.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/yourdomain.com/privkey.pem \
  -out smartdine.p12 \
  -name smartdine

# Copy to project
sudo cp smartdine.p12 src/main/resources/
sudo chown $USER:$USER src/main/resources/smartdine.p12
```

#### Option B: Use existing self-signed certificate (Development only)
- The project already includes a self-signed certificate
- Browsers will show security warnings
- Not recommended for production

### 6. Build and Run

```bash
# Navigate to project directory
cd ~/smartDineBackend

# Build and start containers
docker-compose up -d --build

# View logs
docker-compose logs -f

# Check container status
docker-compose ps
```

### 7. Verify Deployment

```bash
# Check if services are running
docker-compose ps

# Test health endpoint (from VM)
curl -k https://localhost:8443/actuator/health

# Test from outside (replace with VM public IP)
curl -k https://<VM_PUBLIC_IP>:8443/actuator/health
```

### 8. Configure Azure Network Security Group

```bash
# Allow HTTPS traffic
az network nsg rule create \
  --resource-group <your-resource-group> \
  --nsg-name <your-nsg-name> \
  --name AllowHTTPS \
  --priority 1001 \
  --destination-port-ranges 8443 \
  --protocol Tcp \
  --access Allow

# Optional: Allow HTTP traffic
az network nsg rule create \
  --resource-group <your-resource-group> \
  --nsg-name <your-nsg-name> \
  --name AllowHTTP \
  --priority 1002 \
  --destination-port-ranges 8080 \
  --protocol Tcp \
  --access Allow
```

Or configure via Azure Portal:
1. Go to your VM → Networking → Add inbound port rule
2. Add rule for port 8443 (HTTPS)
3. Add rule for port 8080 (HTTP) if needed

## 🐛 Common Issues and Solutions

### Issue 1: "database 'smartDine}' does not exist"
**Cause:** Variable interpolation error in compose.yaml

**Solution:** Already fixed in latest compose.yaml. Make sure you have:
```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/smartDine
```

### Issue 2: Health check failing
**Causes:**
- Spring Boot Actuator not installed
- Actuator endpoint not accessible
- SSL certificate issues

**Solutions:**
```bash
# Check if actuator dependency is in pom.xml
grep "spring-boot-starter-actuator" pom.xml

# Check application logs
docker-compose logs springboot-app | grep -i actuator

# Test with curl ignoring SSL
curl -k -v https://localhost:8443/actuator/health
```

### Issue 3: "Unable to open JDBC Connection"
**Causes:**
- PostgreSQL container not ready
- Incorrect database credentials
- Network issues between containers

**Solutions:**
```bash
# Check PostgreSQL logs
docker-compose logs postgres

# Verify database exists
docker exec -it smartdine-postgres psql -U postgres -l

# Test connection manually
docker exec -it smartdine-postgres psql -U postgres -d smartDine

# Restart with fresh database
docker-compose down -v
docker-compose up -d
```

### Issue 4: "Port already in use"
**Solution:**
```bash
# Find process using the port
sudo lsof -i :8443
sudo lsof -i :5432

# Kill the process or change port in .env
# Then restart
docker-compose down
docker-compose up -d
```

### Issue 5: Container keeps restarting
**Solutions:**
```bash
# Check container logs for errors
docker-compose logs --tail=100 springboot-app

# Check if PostgreSQL is healthy
docker-compose ps

# Increase start_period in healthcheck
# Edit compose.yaml: start_period: 120s
```

### Issue 6: "curl: command not found" in health check
**Cause:** Old Dockerfile without curl installation

**Solution:** Already fixed. Dockerfile now includes:
```dockerfile
RUN apk add --no-cache curl
```

## 🔄 Maintenance Commands

### Update Application
```bash
# Pull latest changes
git pull origin main

# Rebuild and restart
docker-compose down
docker-compose up -d --build
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f springboot-app
docker-compose logs -f postgres

# Last 100 lines
docker-compose logs --tail=100
```

### Database Backup
```bash
# Create backup
docker exec smartdine-postgres pg_dump -U postgres smartDine > backup_$(date +%Y%m%d_%H%M%S).sql

# Restore backup
docker exec -i smartdine-postgres psql -U postgres smartDine < backup_20250109_120000.sql
```

### Clean Up
```bash
# Stop containers (keeps data)
docker-compose down

# Stop and remove volumes (deletes database!)
docker-compose down -v

# Remove unused images
docker image prune -a
```

## 📊 Monitoring

### Check Container Health
```bash
docker-compose ps
docker stats
```

### Check Application Health
```bash
curl -k https://localhost:8443/actuator/health
```

### Monitor Logs in Real-Time
```bash
docker-compose logs -f springboot-app | grep -E "ERROR|WARN|Exception"
```

## 🔐 Security Best Practices for Azure

1. **Use Azure Key Vault** for secrets instead of .env file
2. **Enable Azure DDoS Protection** for production
3. **Configure Azure Application Gateway** with WAF
4. **Use Azure Database for PostgreSQL** instead of container (for production)
5. **Enable Azure Monitor** and Application Insights
6. **Configure automated backups** with Azure Backup
7. **Use Azure Container Registry** for private image storage
8. **Implement Azure Active Directory** authentication
9. **Enable disk encryption** on VM
10. **Configure regular security updates** with automatic patching

## 📱 Testing Endpoints from Outside

### Test Health Check
```bash
curl -k https://<VM_PUBLIC_IP>:8443/actuator/health
```

### Test Authentication
```bash
# Register a new user
curl -k -X POST https://<VM_PUBLIC_IP>:8443/smartdine/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123",
    "phoneNumber": "1234567890"
  }'

# Login
curl -k -X POST https://<VM_PUBLIC_IP>:8443/smartdine/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

## 🎯 Production Deployment Recommendations

For production, consider:

1. **Use Azure Database for PostgreSQL** instead of containerized database
   - Better performance and reliability
   - Automated backups
   - High availability options

2. **Use Azure Container Instances** or **Azure Kubernetes Service (AKS)**
   - Better orchestration
   - Auto-scaling
   - Load balancing

3. **Configure Azure CDN** for static content
4. **Use Azure Front Door** for global load balancing
5. **Implement Azure API Management** for API gateway features
6. **Enable Azure Log Analytics** for centralized logging

## 📞 Support

If you encounter issues:
1. Check this guide's troubleshooting section
2. Review application logs: `docker-compose logs`
3. Check Azure VM metrics in Azure Portal
4. Review PostgreSQL logs: `docker-compose logs postgres`
