# 🐳 Docker Compose - Análisis de Errores para Azure

## 📊 Resumen Ejecutivo

| Categoría | Total | Resueltos | Requiere Acción Usuario |
|-----------|-------|-----------|------------------------|
| 🔴 Críticos | 4 | 4 ✅ | 0 |
| ⚠️ Seguridad | 2 | 0 | 2 ⏳ |
| 💡 Optimización | 4 | 4 ✅ | 0 |
| **TOTAL** | **10** | **8** | **2** |

---

## 🔴 Errores Críticos (Impedían el Despliegue)

### 1. ❌ Error de Base de Datos: `smartDine}` en lugar de `smartDine`
```
ERROR: database "smartDine}" does not exist
```

**Causa:** Interpolación anidada de variables en `compose.yaml`
```yaml
# ❌ INCORRECTO
SPRING_DATASOURCE_URL: ${SPRING_DATASOURCE_URL:-jdbc:postgresql://postgres:5432/${POSTGRES_DB:-smartDine}}
```

**Solución:** ✅ Ruta directa sin anidamiento
```yaml
# ✅ CORRECTO
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/smartDine
```

**Archivo:** `compose.yaml` línea 42  
**Estado:** ✅ RESUELTO

---

### 2. ❌ Falta Dependencia Spring Boot Actuator
```
404 Not Found: /actuator/health
```

**Causa:** La dependencia `spring-boot-starter-actuator` no estaba en `pom.xml`

**Solución:** ✅ Agregada al `pom.xml`
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**Archivo:** `pom.xml`  
**Estado:** ✅ RESUELTO

---

### 3. ❌ Endpoint Actuator Bloqueado por Seguridad
```
403 Forbidden: /actuator/health
```

**Causa:** Spring Security bloqueaba el endpoint de health check

**Solución:** ✅ Permitido acceso público en `SecurityConfig.java`
```java
.requestMatchers("/actuator/health/**").permitAll()
```

**Archivo:** `src/main/java/com/smartDine/configs/SecurityConfig.java`  
**Estado:** ✅ RESUELTO

---

### 4. ❌ Comando Health Check No Disponible
```
exec /bin/sh: wget: not found
```

**Causa:** `wget` no está instalado en la imagen `eclipse-temurin:17-jre-alpine`

**Solución:** ✅ Instalado `curl` y actualizado health check
```dockerfile
# En Dockerfile
RUN apk add --no-cache curl
HEALTHCHECK CMD curl -f -k https://localhost:8443/actuator/health || exit 1
```

```yaml
# En compose.yaml
healthcheck:
  test: ["CMD", "curl", "-f", "-k", "https://localhost:8443/actuator/health"]
```

**Archivos:** `Dockerfile`, `compose.yaml`  
**Estado:** ✅ RESUELTO

---

## ⚠️ Problemas de Seguridad (Requieren Acción)

### 5. ⚠️ Contraseñas por Defecto

**Problema:** Usar contraseñas predeterminadas en producción
- PostgreSQL: `mV00R152`
- JWT Secret: `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970`

**Riesgo:** 🔴 Alto - Acceso no autorizado a base de datos y tokens JWT

**Acción Requerida:**
```bash
# 1. Copiar template
cp .env.example .env

# 2. Generar contraseña segura para PostgreSQL
openssl rand -base64 32

# 3. Generar secret para JWT (256 bits)
openssl rand -hex 32

# 4. Editar .env con los nuevos valores
nano .env
```

**Estado:** ⏳ REQUIERE ACCIÓN DEL USUARIO

---

### 6. ⚠️ Certificado SSL Auto-firmado

**Problema:** Usando certificado `smartdine.p12` auto-firmado

**Riesgo:** ⚠️ Medio - Navegadores mostrarán advertencias de seguridad

**Acción para Producción:**
```bash
# Obtener certificado Let's Encrypt
sudo apt-get install certbot
sudo certbot certonly --standalone -d tudominio.com

# Convertir a PKCS12
sudo openssl pkcs12 -export \
  -in /etc/letsencrypt/live/tudominio.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/tudominio.com/privkey.pem \
  -out smartdine.p12 \
  -name smartdine

# Copiar al proyecto
sudo cp smartdine.p12 src/main/resources/
```

**Estado:** ⏳ REQUIERE ACCIÓN PARA PRODUCCIÓN (OK para desarrollo)

---

## 💡 Optimizaciones Implementadas

### 7. ✅ Template de Variables de Entorno
**Creado:** `.env.example` con todas las variables necesarias  
**Beneficio:** Facilita configuración en nuevos entornos

### 8. ✅ Documentación de Despliegue en Azure
**Creado:** `AZURE-DEPLOYMENT.md` - Guía completa paso a paso  
**Beneficio:** Despliegue rápido y sin errores

### 9. ✅ Script de Despliegue Automatizado
**Creado:** `deploy-azure.sh` - Script bash para automatizar despliegue  
**Beneficio:** Reduce errores manuales

### 10. ✅ Script de Monitoreo
**Creado:** `monitor.sh` - Monitoreo de estado del sistema  
**Beneficio:** Diagnóstico rápido de problemas

---

## 🚀 Instrucciones de Despliegue en Azure

### Requisitos Previos
```bash
# VM recomendada
Tamaño: Standard B2s o superior (2 vCPUs, 4 GB RAM)
OS: Ubuntu 20.04 LTS o posterior

# Puertos a abrir en NSG
22   - SSH
8443 - HTTPS (obligatorio)
8080 - HTTP (opcional)
```

### Despliegue Rápido
```bash
# 1. Conectar a Azure VM
ssh azureuser@<IP_PUBLICA_VM>

# 2. Clonar repositorio
git clone https://github.com/marcmv03/smartDineBackend.git
cd smartDineBackend

# 3. Configurar variables de entorno
cp .env.example .env
nano .env  # Editar con valores de producción

# 4. Ejecutar script de despliegue
chmod +x deploy-azure.sh
./deploy-azure.sh

# 5. Verificar estado
chmod +x monitor.sh
./monitor.sh
```

### Verificación Post-Despliegue
```bash
# Desde dentro de la VM
curl -k https://localhost:8443/actuator/health

# Desde fuera (reemplazar con tu IP)
curl -k https://<IP_PUBLICA_VM>:8443/actuator/health

# Respuesta esperada
{
  "status": "UP"
}
```

---

## 📁 Archivos Creados/Modificados

### Nuevos Archivos
- ✅ `.env.example` - Template de configuración
- ✅ `AZURE-DEPLOYMENT.md` - Guía de despliegue completa
- ✅ `deploy-azure.sh` - Script de despliegue automatizado
- ✅ `monitor.sh` - Script de monitoreo
- ✅ `DOCKER-ISSUES.md` - Análisis detallado de problemas
- ✅ `DOCKER-ISSUES-SUMMARY-ES.md` - Este resumen

### Archivos Modificados
- ✅ `compose.yaml` - Corregida URL de base de datos, health check
- ✅ `Dockerfile` - Instalado curl, actualizado health check
- ✅ `pom.xml` - Agregado spring-boot-starter-actuator
- ✅ `SecurityConfig.java` - Permitido acceso a /actuator/health
- ✅ `application-prod.properties` - Configuración de puerto y SSL
- ✅ `README-DOCKER.md` - Documentación actualizada con HTTPS

---

## ✅ Checklist Final para Producción

### Antes de Desplegar
- [x] Todos los errores críticos resueltos
- [ ] Generar contraseñas seguras (PostgreSQL, JWT)
- [ ] Obtener certificado SSL válido (Let's Encrypt)
- [ ] Configurar .env con valores de producción
- [x] Revisar AZURE-DEPLOYMENT.md

### Durante el Despliegue
- [ ] VM Azure con recursos suficientes (B2s+)
- [ ] Puertos abiertos en NSG (22, 8443)
- [ ] Docker y Docker Compose instalados
- [ ] Ejecutar deploy-azure.sh
- [ ] Verificar health endpoint

### Después del Despliegue
- [ ] Probar health endpoint desde exterior
- [ ] Revisar logs: `docker-compose logs -f`
- [ ] Ejecutar monitor.sh periódicamente
- [ ] Configurar backups de PostgreSQL
- [ ] Configurar monitoreo con Azure Monitor

---

## 🔧 Comandos Útiles

### Gestión de Contenedores
```bash
# Ver estado
docker-compose ps

# Ver logs en tiempo real
docker-compose logs -f

# Reiniciar servicios
docker-compose restart

# Parar servicios
docker-compose down

# Parar y eliminar datos (⚠️ cuidado)
docker-compose down -v

# Reconstruir y reiniciar
docker-compose up -d --build
```

### Monitoreo
```bash
# Ejecutar script de monitoreo
./monitor.sh

# Ver uso de recursos
docker stats

# Ver salud de contenedores
docker inspect --format='{{.State.Health.Status}}' smartdine-backend
docker inspect --format='{{.State.Health.Status}}' smartdine-postgres
```

### Base de Datos
```bash
# Conectar a PostgreSQL
docker exec -it smartdine-postgres psql -U postgres -d smartDine

# Backup
docker exec smartdine-postgres pg_dump -U postgres smartDine > backup.sql

# Restore
docker exec -i smartdine-postgres psql -U postgres smartDine < backup.sql
```

---

## 📞 Soporte

### Documentación
1. **AZURE-DEPLOYMENT.md** - Guía completa de despliegue
2. **DOCKER-ISSUES.md** - Análisis detallado técnico
3. **README-DOCKER.md** - Comandos Docker básicos

### Troubleshooting
```bash
# Paso 1: Revisar estado
./monitor.sh

# Paso 2: Ver logs
docker-compose logs -f springboot-app

# Paso 3: Verificar variables
docker exec smartdine-backend env | grep SPRING

# Paso 4: Probar health endpoint
curl -k -v https://localhost:8443/actuator/health
```

---

## 📊 Estado del Proyecto

| Aspecto | Estado | Notas |
|---------|--------|-------|
| Docker Build | ✅ OK | Multi-stage build optimizado |
| Database Connection | ✅ OK | PostgreSQL 17.6 |
| Health Checks | ✅ OK | Curl instalado y configurado |
| HTTPS/SSL | ✅ OK | Puerto 8443, cert auto-firmado |
| Security | ⚠️ Parcial | Cambiar contraseñas para producción |
| Actuator | ✅ OK | Endpoint público habilitado |
| Documentation | ✅ OK | Guías completas creadas |
| Monitoring | ✅ OK | Script de monitoreo disponible |
| Azure Ready | ✅ OK | Listo para despliegue |

**Conclusión:** El proyecto está **LISTO PARA DESPLIEGUE EN AZURE** ✅

Solo se requiere configurar contraseñas de producción y opcionalmente obtener certificado SSL válido.

---

**Última Actualización:** 9 de Enero, 2025  
**Versión:** 1.0  
**Estado:** ✅ Listo para Producción (con acciones pendientes de seguridad)
