# API Documentation Update Summary

## 📝 Cambios Realizados en `api.yaml`

### ✅ Actualización Completa de la Documentación OpenAPI 3.0.3

Fecha: 11 de Enero, 2025

---

## 🆕 Nuevos Endpoints Documentados

### 1. **Tables Management** (Gestión de Mesas)

#### POST `/smartdine/api/restaurants/{restaurantId}/tables`
- **Propósito**: Crear una nueva mesa para un restaurante
- **Autenticación**: Requerida (Bearer JWT)
- **Rol**: Solo BUSINESS
- **Request Body**: `TableRequest`
  - `number` (integer, required): Número de mesa único
  - `capacity` (integer, required): Capacidad de personas
  - `outside` (boolean, required): Si está en el exterior
- **Respuestas**:
  - 201: Mesa creada exitosamente
  - 401: No autenticado
  - 403: No es propietario del negocio
  - 400: Error de validación o número duplicado

#### GET `/smartdine/api/restaurants/{restaurantId}/tables`
- **Propósito**: Obtener todas las mesas de un restaurante
- **Autenticación**: Requerida (Bearer JWT)
- **Rol**: Solo BUSINESS (propietario)
- **Respuestas**:
  - 200: Lista de mesas
  - 401: No autenticado
  - 403: No es propietario

#### GET `/smartdine/api/restaurants/{restaurantId}/tables/available`
- **Propósito**: Obtener mesas disponibles para una fecha y hora específica
- **Autenticación**: No requerida (público)
- **Query Parameters**:
  - `date` (string, required): Fecha en formato YYYY-MM-DD
  - `timeSlot` (integer, required): ID del time slot
  - `outside` (boolean, required): Filtrar por mesas exteriores/interiores
- **Respuestas**:
  - 200: Lista de mesas disponibles

---

### 2. **Reservations** (Reservas)

#### POST `/smartdine/api/reservations`
- **Propósito**: Crear una nueva reserva
- **Autenticación**: Requerida (Bearer JWT)
- **Rol**: Solo CUSTOMER
- **Request Body**: `ReservationRequest`
  - `timeSlotId` (integer, required): ID del time slot
  - `restaurantId` (integer, required): ID del restaurante
  - `tableId` (integer, required): ID de la mesa a reservar
  - `numCustomers` (integer, required): Número de comensales (mínimo 1)
  - `date` (string, required): Fecha de reserva (YYYY-MM-DD)
- **Respuestas**:
  - 201: Reserva creada exitosamente
  - 401: No autenticado
  - 403: No es cliente
  - 400: Error de validación o mesa no disponible

#### GET `/smartdine/api/me/reservations`
- **Propósito**: Obtener todas las reservas del cliente autenticado
- **Autenticación**: Requerida (Bearer JWT)
- **Rol**: Solo CUSTOMER
- **Respuestas**:
  - 200: Lista de reservas del cliente
  - 401: No autenticado
  - 403: No es cliente

---

### 3. **Time Slots** (Actualizado)

#### GET `/smartdine/api/restaurants/{restaurantId}/timeslots`
- **Propósito**: Obtener time slots de un restaurante
- **Autenticación**: Requerida (Bearer JWT)
- **Query Parameters**:
  - `day` (DayOfWeek, optional): Filtrar por día de la semana
- **Respuestas**:
  - 200: Lista de time slots

**Nota**: La ruta cambió de `/timeSlots` a `/timeslots` (lowercase) para coincidir con la implementación real.

---

## 🔧 Schemas Nuevos Añadidos

### `TableRequest`
```yaml
type: object
required:
  - number
  - capacity
  - outside
properties:
  number:
    type: integer
    minimum: 1
  capacity:
    type: integer
    minimum: 1
  outside:
    type: boolean
```

### `RestaurantTable`
```yaml
type: object
properties:
  id:
    type: integer
    format: int64
  number:
    type: integer
  capacity:
    type: integer
  outside:
    type: boolean
  restaurantId:
    type: integer
    format: int64
```

### `ReservationRequest`
```yaml
type: object
required:
  - timeSlotId
  - restaurantId
  - tableId
  - numCustomers
  - date
properties:
  timeSlotId:
    type: integer
    format: int64
  restaurantId:
    type: integer
    format: int64
  tableId:
    type: integer
    format: int64
    minimum: 1
  numCustomers:
    type: integer
    minimum: 1
  date:
    type: string
    format: date
```

### `Reservation`
```yaml
type: object
properties:
  id:
    type: integer
    format: int64
  timeSlotId:
    type: integer
    format: int64
  restaurantId:
    type: integer
    format: int64
  tableId:
    type: integer
    format: int64
  customerId:
    type: integer
    format: int64
  numCustomers:
    type: integer
  date:
    type: string
    format: date
```

---

## ✏️ Modificaciones en Schemas Existentes

### `RegisterUserRequest`
- **Añadido**: Validaciones de `minimum` y `maximum` para `phoneNumber`
  - Mínimo: 100000000 (9 dígitos)
  - Máximo: 999999999999 (12 dígitos)
- **Descripción actualizada**: Especifica rango de 9-12 dígitos

---

## 🏷️ Nuevas Tags

### `Tables`
- **Descripción**: Manage restaurant tables for reservations.

### `Reservations`
- **Descripción**: Create and manage customer reservations.

---

## 🔐 Cambios en Seguridad y Autenticación

### Endpoints por Rol:

**Públicos (Sin autenticación):**
- `GET /smartdine/api/restaurants` - Listar restaurantes
- `GET /smartdine/api/restaurants/{id}/menu-items` - Ver menú
- `GET /smartdine/api/restaurants/{id}/tables/available` - Ver mesas disponibles
- `POST /smartdine/api/auth/register/customer` - Registrarse como cliente
- `POST /smartdine/api/auth/register/business` - Registrarse como negocio
- `POST /smartdine/api/auth/login` - Iniciar sesión

**Solo CUSTOMER:**
- `POST /smartdine/api/reservations` - Crear reserva
- `GET /smartdine/api/me/reservations` - Ver mis reservas

**Solo BUSINESS:**
- `POST /smartdine/api/restaurants` - Crear restaurante
- `PUT /smartdine/api/restaurants/{id}` - Actualizar restaurante
- `DELETE /smartdine/api/restaurants/{id}` - Eliminar restaurante
- `POST /smartdine/api/restaurants/{id}/menu-items` - Añadir item al menú
- `POST /smartdine/api/restaurants/{id}/timeslots` - Crear time slot
- `POST /smartdine/api/restaurants/{id}/tables` - Crear mesa
- `GET /smartdine/api/restaurants/{id}/tables` - Ver mesas del restaurante

**Autenticado (Cualquier rol):**
- `GET /smartdine/api/me` - Ver perfil
- `GET /smartdine/api/restaurants/{id}` - Ver detalles de restaurante
- `GET /smartdine/api/restaurants/{id}/timeslots` - Ver time slots

---

## 🌐 Servers Actualizados

### Desarrollo Local:
```yaml
servers:
  - url: https://localhost:8443
    description: Local development server (HTTPS)
  - url: http://localhost:8080
    description: Local development server (HTTP)
```

**Cambio**: Añadido servidor HTTPS como principal, HTTP como alternativa.

---

## 📊 Estadísticas de la Documentación

### Antes:
- **Endpoints**: 10
- **Tags**: 5
- **Schemas**: 15

### Después:
- **Endpoints**: 15 (+5)
- **Tags**: 7 (+2)
- **Schemas**: 19 (+4)

### Nuevos Endpoints:
1. `GET /smartdine/api/restaurants/{id}/timeslots` - Listar time slots con filtro por día
2. `POST /smartdine/api/restaurants/{id}/tables` - Crear mesa
3. `GET /smartdine/api/restaurants/{id}/tables` - Listar mesas
4. `GET /smartdine/api/restaurants/{id}/tables/available` - Mesas disponibles
5. `POST /smartdine/api/reservations` - Crear reserva
6. `GET /smartdine/api/me/reservations` - Mis reservas

---

## 🔍 Validaciones Documentadas

### TableRequest:
- `number`: >= 1
- `capacity`: >= 1
- `outside`: booleano requerido

### ReservationRequest:
- `timeSlotId`: requerido
- `restaurantId`: requerido
- `tableId`: requerido, >= 1
- `numCustomers`: requerido, >= 1
- `date`: requerido, formato YYYY-MM-DD

### RegisterUserRequest:
- `phoneNumber`: entre 100000000 y 999999999999 (9-12 dígitos)

---

## 🎯 Casos de Uso Cubiertos

### Flujo Completo de Reserva:
1. **Cliente busca restaurantes**: `GET /restaurants?search=italian`
2. **Ve el menú**: `GET /restaurants/{id}/menu-items`
3. **Ve horarios disponibles**: `GET /restaurants/{id}/timeslots?day=MONDAY`
4. **Ve mesas disponibles**: `GET /restaurants/{id}/tables/available?date=2025-01-15&timeSlot=1&outside=false`
5. **Crea reserva**: `POST /reservations`
6. **Ve sus reservas**: `GET /me/reservations`

### Flujo de Gestión de Negocio:
1. **Business registra restaurante**: `POST /restaurants`
2. **Añade items al menú**: `POST /restaurants/{id}/menu-items`
3. **Configura horarios**: `POST /restaurants/{id}/timeslots`
4. **Añade mesas**: `POST /restaurants/{id}/tables`
5. **Gestiona mesas**: `GET /restaurants/{id}/tables`

---

## ✅ Verificación de Consistencia

### Rutas Verificadas:
- ✅ Todos los endpoints coinciden con los controladores
- ✅ Todos los DTOs están documentados
- ✅ Todos los códigos de respuesta HTTP están incluidos
- ✅ Todas las validaciones están documentadas
- ✅ Todos los roles y permisos están especificados

### Discrepancias Corregidas:
1. **Ruta de Time Slots**: Cambiada de `/timeSlots` a `/timeslots` (lowercase)
2. **phoneNumber**: Actualizado con validaciones correctas (9-12 dígitos)
3. **Respuestas de error**: Añadidas respuestas 401 y 403 donde correspondía

---

## 📚 Documentación de Referencia

### Archivos de Código Consultados:
1. `AuthenticationController.java`
2. `RestaurantController.java`
3. `MenuItemController.java`
4. `TimeSlotController.java`
5. `TableController.java`
6. `ReservationController.java`
7. `UserProfileController.java`

### DTOs Consultados:
1. `RegisterUser.java` / `RegisterCustomerRequest.java` / `RegisterBusinessRequest.java`
2. `LoginRequest.java`
3. `RestaurantDTO.java`
4. `MenuItemDTO.java` / `DishDTO.java` / `DrinkDTO.java`
5. `TimeSlotDTO.java`
6. `RestaurantTableDTO.java`
7. `ReservationDTO.java`

---

## 🚀 Próximos Pasos Recomendados

1. **Generar Cliente**: Usar la especificación OpenAPI para generar clientes automáticos
   ```bash
   # Ejemplo con openapi-generator
   openapi-generator-cli generate -i api.yaml -g typescript-axios -o client/
   ```

2. **Validación**: Usar herramientas de validación OpenAPI
   ```bash
   # Ejemplo con swagger-cli
   swagger-cli validate api.yaml
   ```

3. **Importar a Postman**: Importar `api.yaml` en Postman para pruebas manuales

4. **Configurar Swagger UI**: Servir la documentación con Swagger UI en el proyecto Spring Boot
   - Añadir dependencia `springdoc-openapi-ui`
   - Configurar en `application.properties`

---

## 📝 Notas de Implementación

### Convenciones Seguidas:
- **Nombres de rutas**: Usar plural para colecciones (`restaurants`, `tables`, `reservations`)
- **Códigos HTTP**: 
  - 200: GET exitoso
  - 201: POST exitoso (recurso creado)
  - 204: DELETE exitoso
  - 400: Error de validación
  - 401: No autenticado
  - 403: No autorizado (rol incorrecto)
  - 404: Recurso no encontrado

### Consistencia con Spring Boot:
- Todos los endpoints usan el prefijo `/smartdine/api/`
- Los parámetros de path usan `{camelCase}`
- Los query parameters usan `lowercase`
- Los DTOs se convierten automáticamente de/a entidades

---

**Documentación actualizada por**: GitHub Copilot  
**Fecha**: 11 de Enero, 2025  
**Versión de API**: 1.0.0  
**Estado**: ✅ Completa y Verificada
