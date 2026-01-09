# Análisis de Endpoints Faltantes para Aplicación Móvil
**SmartDine Backend - API REST**

**Fecha:** 07 de enero de 2026
**Versión:** 1.0
**Analista:** Arquitectura de Software
**Archivo Analizado:** `api.yaml`

---

## 📊 RESUMEN EJECUTIVO

Se ha realizado un análisis exhaustivo del archivo `api.yaml` comparándolo con las 5 funcionalidades requeridas para la aplicación móvil. Se han identificado **8 endpoints faltantes críticos** que bloquean la implementación completa de las funcionalidades especificadas.

### Estado de Implementación por Funcionalidad

| Funcionalidad | Endpoints Existentes | Endpoints Faltantes | Estado |
|--------------|---------------------|---------------------|--------|
| 1. Búsqueda de usuarios y peticiones de amistad | 2/3 | 1 | 🟡 67% |
| 2. Notificaciones y gestión de solicitudes | 4/4 | 0 | ✅ 100% |
| 3. Añadir amigos a reservas como participantes | 0/2 | 2 | 🔴 0% |
| 4. Solicitar unirse a comunidad privada | 0/3 | 3 | 🔴 0% |
| 5. Eliminar amigos y participantes | 1/2 | 1 | 🟡 50% |

**Resumen General:**
- ✅ **Endpoints Implementados:** 7/14 (50%)
- 🔴 **Endpoints Faltantes Críticos:** 7/14 (50%)

---

## 📋 ANÁLISIS DETALLADO POR FUNCIONALIDAD

---

## 1️⃣ FUNCIONALIDAD 1: Búsqueda de Usuarios y Peticiones de Amistad

### Descripción
Un usuario debe poder buscar otros usuarios del sistema y enviarles peticiones de amistad.

### ✅ Endpoints Existentes (2/3)

#### 1.1 Enviar Petición de Amistad
```yaml
POST /smartdine/api/users/{id}/friend-requests
```
**Estado:** ✅ IMPLEMENTADO
**Ubicación en api.yaml:** Líneas 836-874
**Implementado en:** `FriendController.java:52-67`

**Respuesta:**
```json
{
  "id": 123,
  "senderId": 1,
  "senderName": "Juan Pérez",
  "receiverId": 2,
  "receiverName": "María García",
  "requestType": "FRIEND_REQUEST",
  "status": "PENDING",
  "createdAt": "2026-01-07T10:30:00"
}
```

**Validaciones implementadas:**
- ✅ Usuario autenticado (Bearer token)
- ✅ Solo usuarios con rol CUSTOMER pueden enviar peticiones
- ✅ No puede enviar petición a sí mismo
- ✅ No permite peticiones duplicadas pendientes
- ✅ No permite peticiones si ya son amigos

#### 1.2 Obtener Peticiones Pendientes Recibidas
```yaml
GET /smartdine/api/users/me/friend-requests
```
**Estado:** ✅ IMPLEMENTADO
**Ubicación en api.yaml:** Líneas 876-896
**Implementado en:** `FriendController.java:73-86`

**Respuesta:**
```json
[
  {
    "id": 123,
    "senderId": 5,
    "senderName": "Carlos López",
    "senderEmail": "carlos@example.com",
    "receiverId": 1,
    "receiverName": "Juan Pérez",
    "requestType": "FRIEND_REQUEST",
    "status": "PENDING",
    "createdAt": "2026-01-07T09:15:00"
  }
]
```

---

### 🔴 Endpoints Faltantes (1/3)

#### 1.3 🔴 FALTANTE: Buscar Usuarios
```yaml
GET /smartdine/api/users/search
```

**Prioridad:** 🔴 CRÍTICA
**Bloquea:** Funcionalidad completa de búsqueda de usuarios

**Propuesta de Especificación:**

```yaml
/smartdine/api/users/search:
  get:
    tags:
      - Users
    summary: Search for users by name or email
    operationId: searchUsers
    security:
      - bearerAuth: []
    parameters:
      - in: query
        name: query
        required: true
        schema:
          type: string
          minLength: 3
        description: Search term (minimum 3 characters) to filter by name or email
        example: "juan"
      - in: query
        name: page
        required: false
        schema:
          type: integer
          default: 0
        description: Page number for pagination
      - in: query
        name: size
        required: false
        schema:
          type: integer
          default: 20
          maximum: 100
        description: Number of results per page
    responses:
      '200':
        description: List of users matching the search criteria
        content:
          application/json:
            schema:
              type: object
              properties:
                users:
                  type: array
                  items:
                    $ref: '#/components/schemas/UserSearchResult'
                totalElements:
                  type: integer
                totalPages:
                  type: integer
                currentPage:
                  type: integer
      '400':
        description: Bad request - search term too short
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
      '401':
        description: Unauthorized - authentication required
      '403':
        description: Forbidden - only customers can search users
```

**Schema Requerido:**
```yaml
UserSearchResult:
  type: object
  properties:
    id:
      type: integer
      format: int64
    name:
      type: string
    email:
      type: string
      format: email
    isFriend:
      type: boolean
      description: Whether the authenticated user is already friends with this user
    hasPendingRequest:
      type: boolean
      description: Whether there's a pending friend request between the users
```

**Ejemplo de Respuesta:**
```json
{
  "users": [
    {
      "id": 42,
      "name": "Juan Martínez",
      "email": "juan.martinez@example.com",
      "isFriend": false,
      "hasPendingRequest": false
    },
    {
      "id": 88,
      "name": "Juana Pérez",
      "email": "juana@example.com",
      "isFriend": true,
      "hasPendingRequest": false
    }
  ],
  "totalElements": 15,
  "totalPages": 1,
  "currentPage": 0
}
```

**Consideraciones de Implementación:**
1. **Búsqueda:** Case-insensitive, buscar en `name` y `email`
2. **Filtros:** Excluir al usuario autenticado de los resultados
3. **Privacidad:** Solo mostrar usuarios con rol CUSTOMER
4. **Performance:** Indexar columnas `name` y `email` en la base de datos
5. **Paginación:** Implementar usando Spring Data `Pageable`

**Query Repository Sugerida:**
```java
@Query("SELECT c FROM Customer c WHERE " +
       "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
       "LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')) " +
       "AND c.id != :excludeId")
Page<Customer> searchCustomers(@Param("query") String query,
                                @Param("excludeId") Long excludeId,
                                Pageable pageable);
```

---

## 2️⃣ FUNCIONALIDAD 2: Notificaciones y Solicitudes de Amistad

### Descripción
El usuario puede ver sus notificaciones, algunas de las cuales son solicitudes de amistad que puede aceptar o rechazar.

### ✅ Estado: 100% IMPLEMENTADO

Todos los endpoints necesarios están implementados correctamente:

#### 2.1 ✅ Obtener Notificaciones
```yaml
GET /smartdine/api/users/me/notifications
```
**Ubicación:** Líneas 1029-1047 de api.yaml
**Respuesta:** Lista de notificaciones ordenadas por fecha (más recientes primero)

#### 2.2 ✅ Marcar Notificación como Leída
```yaml
POST /smartdine/api/notifications/{id}/read
```
**Ubicación:** Líneas 1049-1082 de api.yaml

#### 2.3 ✅ Aceptar Solicitud de Amistad
```yaml
POST /smartdine/api/friend-requests/{id}/accept
```
**Ubicación:** Líneas 898-934 de api.yaml
**Implementado en:** `FriendController.java:92-106`

**Efecto:**
- Cambia el estado del Request a `ACCEPTED`
- Crea una entidad `Friendship` bidireccional

#### 2.4 ✅ Rechazar Solicitud de Amistad
```yaml
POST /smartdine/api/friend-requests/{id}/reject
```
**Ubicación:** Líneas 936-972 de api.yaml
**Implementado en:** `FriendController.java:112-126`

**Efecto:**
- Cambia el estado del Request a `REJECTED`
- No se crea amistad

### ✅ Integración Completa

El flujo completo está soportado:
1. Usuario A envía friend request a Usuario B
2. Se crea una notificación para Usuario B con `requestId`
3. Usuario B obtiene sus notificaciones y ve la solicitud
4. Usuario B puede aceptar o rechazar usando el `requestId`
5. La notificación puede marcarse como leída

---

## 3️⃣ FUNCIONALIDAD 3: Añadir Amigos a Reservas como Participantes

### Descripción
Un cliente puede añadir amigos (que también sean clientes) a una reserva como participantes, respetando restricciones de capacidad y evitando duplicados.

### 🔴 Estado: 0% IMPLEMENTADO

**Endpoints existentes relacionados:**
- ✅ `GET /smartdine/api/users/me/friends` - Listar amigos
- ✅ `GET /smartdine/api/reservations/{id}/participants` - Ver participantes (implementado en ReservationController.java:119-135)

### 🔴 Endpoints Faltantes (2/2)

---

#### 3.1 🔴 FALTANTE: Añadir Participante a Reserva
```yaml
POST /smartdine/api/reservations/{reservationId}/participants
```

**Prioridad:** 🔴 CRÍTICA
**Bloquea:** Funcionalidad completa de participación en reservas

**Propuesta de Especificación:**

```yaml
/smartdine/api/reservations/{reservationId}/participants:
  post:
    tags:
      - Reservations
    summary: Add a friend as a participant to an existing reservation
    description: |
      Allows the reservation owner to add a friend as a participant.
      Validations:
      - Requester must be the owner of the reservation
      - User to add must be a friend of the owner
      - User to add must be a CUSTOMER
      - Cannot add the same user twice
      - Cannot exceed table capacity
      - Participant cannot have time conflicts with other reservations
    operationId: addParticipantToReservation
    security:
      - bearerAuth: []
    parameters:
      - in: path
        name: reservationId
        required: true
        schema:
          type: integer
          format: int64
        description: ID of the reservation
    requestBody:
      required: true
      content:
        application/json:
          schema:
            type: object
            required:
              - friendId
            properties:
              friendId:
                type: integer
                format: int64
                description: ID of the friend to add as participant
          example:
            friendId: 42
    responses:
      '201':
        description: Participant added successfully
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ReservationParticipation'
      '400':
        description: |
          Bad request - one of:
          - User is not a friend
          - User already a participant
          - Would exceed table capacity
          - Participant has time conflict
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
      '401':
        description: Unauthorized
      '403':
        description: |
          Forbidden - one of:
          - User is not a CUSTOMER
          - User is not the owner of the reservation
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
      '404':
        description: Reservation or friend not found
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
```

**Schema Requerido:**
```yaml
ReservationParticipation:
  type: object
  properties:
    id:
      type: integer
      format: int64
    reservationId:
      type: integer
      format: int64
    participantId:
      type: integer
      format: int64
    participantName:
      type: string
    addedAt:
      type: string
      format: date-time
```

**Ejemplo de Respuesta:**
```json
{
  "id": 1,
  "reservationId": 123,
  "participantId": 42,
  "participantName": "Juan Martínez",
  "addedAt": "2026-01-07T11:00:00"
}
```

**Validaciones a Implementar:**
```java
// En ReservationController (nuevo método)
@PostMapping("/reservations/{reservationId}/participants")
public ResponseEntity<ReservationParticipationDTO> addParticipant(
    @PathVariable Long reservationId,
    @Valid @RequestBody AddParticipantRequest request,
    @AuthenticationPrincipal User user
) {
    // 1. Verificar autenticación y rol CUSTOMER
    // 2. Verificar que es el owner de la reserva
    // 3. Verificar que friendId es realmente amigo
    // 4. Delegar validaciones complejas al service
    // 5. Retornar DTO de participación creada
}
```

---

#### 3.2 🔴 FALTANTE: Listar Amigos Elegibles para Añadir
```yaml
GET /smartdine/api/reservations/{reservationId}/eligible-friends
```

**Prioridad:** 🟡 ALTA (facilita UX en móvil)
**Bloquea:** Experiencia de usuario óptima

**Propuesta de Especificación:**

```yaml
/smartdine/api/reservations/{reservationId}/eligible-friends:
  get:
    tags:
      - Reservations
    summary: Get list of friends that can be added to the reservation
    description: |
      Returns friends of the reservation owner that:
      - Are not already participants
      - Don't have time conflicts
      - Reservation has capacity available
    operationId: getEligibleFriends
    security:
      - bearerAuth: []
    parameters:
      - in: path
        name: reservationId
        required: true
        schema:
          type: integer
          format: int64
        description: ID of the reservation
    responses:
      '200':
        description: List of friends that can be added
        content:
          application/json:
            schema:
              type: object
              properties:
                eligibleFriends:
                  type: array
                  items:
                    $ref: '#/components/schemas/EligibleFriend'
                availableSlots:
                  type: integer
                  description: Number of participants that can still be added
      '401':
        description: Unauthorized
      '403':
        description: Forbidden - user is not the owner of the reservation
      '404':
        description: Reservation not found
```

**Schema Requerido:**
```yaml
EligibleFriend:
  type: object
  properties:
    friendId:
      type: integer
      format: int64
    friendName:
      type: string
    friendEmail:
      type: string
      format: email
    canBeAdded:
      type: boolean
      description: Whether this friend can be added (no conflicts)
    conflictReason:
      type: string
      nullable: true
      description: Reason why friend cannot be added (if canBeAdded is false)
      enum:
        - null
        - "TIME_CONFLICT"
        - "ALREADY_PARTICIPANT"
```

**Ejemplo de Respuesta:**
```json
{
  "eligibleFriends": [
    {
      "friendId": 42,
      "friendName": "Juan Martínez",
      "friendEmail": "juan@example.com",
      "canBeAdded": true,
      "conflictReason": null
    },
    {
      "friendId": 88,
      "friendName": "María López",
      "friendEmail": "maria@example.com",
      "canBeAdded": false,
      "conflictReason": "TIME_CONFLICT"
    }
  ],
  "availableSlots": 2
}
```

---

## 4️⃣ FUNCIONALIDAD 4: Solicitar Unirse a Comunidad Privada

### Descripción
Un usuario debe poder solicitar unirse a una comunidad privada. Se crea un `CommunityJoinRequest` asociado a la comunidad y se envía al propietario.

### 🔴 Estado: 0% IMPLEMENTADO

**Análisis de la Entidad Request:**
La entidad `Request` (líneas 23-94 de Request.java) ya soporta múltiples tipos mediante el enum `RequestType`:
```java
@Enumerated(EnumType.STRING)
private RequestType requestType; // FRIEND_REQUEST, COMMUNITY_JOIN_REQUEST, etc.
```

**Problema:** El enum `RequestType` actual solo tiene `FRIEND_REQUEST` definido. Se necesita añadir `COMMUNITY_JOIN_REQUEST`.

### 🔴 Endpoints Faltantes (3/3)

---

#### 4.1 🔴 FALTANTE: Solicitar Unirse a Comunidad
```yaml
POST /smartdine/api/communities/{communityId}/join-requests
```

**Prioridad:** 🔴 CRÍTICA
**Bloquea:** Sistema de comunidades privadas

**Propuesta de Especificación:**

```yaml
/smartdine/api/communities/{communityId}/join-requests:
  post:
    tags:
      - Communities
    summary: Request to join a private community
    description: |
      Creates a join request for a private community.
      The request is sent to the community owner.
      Validations:
      - Community must exist and be private
      - User must not already be a member
      - User must not have a pending join request
    operationId: requestToJoinCommunity
    security:
      - bearerAuth: []
    parameters:
      - in: path
        name: communityId
        required: true
        schema:
          type: integer
          format: int64
        description: ID of the community to join
    requestBody:
      required: false
      content:
        application/json:
          schema:
            type: object
            properties:
              message:
                type: string
                maxLength: 500
                description: Optional message to the community owner
          example:
            message: "Hi! I'd love to join your community."
    responses:
      '201':
        description: Join request created successfully
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CommunityJoinRequest'
      '400':
        description: |
          Bad request - one of:
          - Community is public (no request needed)
          - User already a member
          - Pending request already exists
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
      '401':
        description: Unauthorized
      '403':
        description: Forbidden - only customers can join communities
      '404':
        description: Community not found
```

**Schema Requerido:**
```yaml
CommunityJoinRequest:
  type: object
  properties:
    id:
      type: integer
      format: int64
    requesterId:
      type: integer
      format: int64
    requesterName:
      type: string
    communityId:
      type: integer
      format: int64
    communityName:
      type: string
    ownerId:
      type: integer
      format: int64
    message:
      type: string
      nullable: true
    requestType:
      type: string
      enum:
        - COMMUNITY_JOIN_REQUEST
    status:
      type: string
      enum:
        - PENDING
        - ACCEPTED
        - REJECTED
    createdAt:
      type: string
      format: date-time
```

**Modificación Requerida en Request.java:**
```java
// Añadir campo para referencia a comunidad
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "community_id", nullable = true)
private Community community;  // Solo para COMMUNITY_JOIN_REQUEST

// Añadir campo para mensaje opcional
@Column(length = 500)
private String message;
```

**Modificación Requerida en RequestType.java:**
```java
public enum RequestType {
    FRIEND_REQUEST,
    COMMUNITY_JOIN_REQUEST  // NUEVO
}
```

---

#### 4.2 🔴 FALTANTE: Obtener Solicitudes de Unión Pendientes (Owner)
```yaml
GET /smartdine/api/communities/{communityId}/join-requests
```

**Prioridad:** 🔴 CRÍTICA
**Bloquea:** Gestión de comunidades privadas

**Propuesta de Especificación:**

```yaml
/smartdine/api/communities/{communityId}/join-requests:
  get:
    tags:
      - Communities
    summary: Get pending join requests for a community
    description: |
      Returns all pending join requests for a community.
      Only the community owner or administrators can access this endpoint.
    operationId: getCommunityJoinRequests
    security:
      - bearerAuth: []
    parameters:
      - in: path
        name: communityId
        required: true
        schema:
          type: integer
          format: int64
        description: ID of the community
      - in: query
        name: status
        required: false
        schema:
          type: string
          enum:
            - PENDING
            - ACCEPTED
            - REJECTED
          default: PENDING
        description: Filter by request status
    responses:
      '200':
        description: List of join requests
        content:
          application/json:
            schema:
              type: array
              items:
                $ref: '#/components/schemas/CommunityJoinRequest'
      '401':
        description: Unauthorized
      '403':
        description: Forbidden - user is not the owner or admin of the community
      '404':
        description: Community not found
```

**Ejemplo de Respuesta:**
```json
[
  {
    "id": 15,
    "requesterId": 42,
    "requesterName": "Juan Martínez",
    "communityId": 5,
    "communityName": "Amantes de la Pizza",
    "ownerId": 1,
    "message": "Me encanta la pizza, quiero unirme!",
    "requestType": "COMMUNITY_JOIN_REQUEST",
    "status": "PENDING",
    "createdAt": "2026-01-07T10:30:00"
  }
]
```

---

#### 4.3 🔴 FALTANTE: Aceptar/Rechazar Solicitud de Unión
```yaml
POST /smartdine/api/community-join-requests/{id}/accept
POST /smartdine/api/community-join-requests/{id}/reject
```

**Prioridad:** 🔴 CRÍTICA
**Bloquea:** Gestión completa de comunidades privadas

**Propuesta de Especificación:**

```yaml
/smartdine/api/community-join-requests/{id}/accept:
  post:
    tags:
      - Communities
    summary: Accept a community join request
    description: |
      Accepts a join request and adds the user as a PARTICIPANT member.
      Only the community owner or administrators can accept requests.
    operationId: acceptCommunityJoinRequest
    security:
      - bearerAuth: []
    parameters:
      - in: path
        name: id
        required: true
        schema:
          type: integer
          format: int64
        description: ID of the join request
    responses:
      '200':
        description: Join request accepted, user added to community
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CommunityJoinRequest'
      '400':
        description: Request is not pending or already processed
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
      '401':
        description: Unauthorized
      '403':
        description: Forbidden - user is not the owner or admin of the community
      '404':
        description: Request not found

/smartdine/api/community-join-requests/{id}/reject:
  post:
    tags:
      - Communities
    summary: Reject a community join request
    description: |
      Rejects a join request without adding the user to the community.
      Only the community owner or administrators can reject requests.
    operationId: rejectCommunityJoinRequest
    security:
      - bearerAuth: []
    parameters:
      - in: path
        name: id
        required: true
        schema:
          type: integer
          format: int64
        description: ID of the join request
    responses:
      '200':
        description: Join request rejected
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CommunityJoinRequest'
      '400':
        description: Request is not pending or already processed
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
      '401':
        description: Unauthorized
      '403':
        description: Forbidden - user is not the owner or admin of the community
      '404':
        description: Request not found
```

**Efecto de Aceptar:**
1. Cambiar `status` del Request a `ACCEPTED`
2. Crear una entidad `Member` con rol `PARTICIPANT`
3. Añadir a `community.members`
4. Crear notificación para el solicitante

**Efecto de Rechazar:**
1. Cambiar `status` del Request a `REJECTED`
2. Opcionalmente crear notificación para el solicitante

---

## 5️⃣ FUNCIONALIDAD 5: Eliminar Amigos y Participantes de Reservas

### Descripción
Un usuario debe poder eliminar amigos de su lista y eliminar participantes de sus reservas.

### ✅ Endpoint Existente (1/2)

#### 5.1 ✅ Eliminar Amigo
```yaml
DELETE /smartdine/api/friends/{friendId}
```
**Estado:** ✅ IMPLEMENTADO
**Ubicación en api.yaml:** Líneas 996-1026
**Implementado en:** `FriendController.java:152-167`

**Efecto:**
- Elimina la relación `Friendship` bidireccional
- Ambos usuarios dejan de ser amigos

---

### 🔴 Endpoint Faltante (1/2)

#### 5.2 🔴 FALTANTE: Eliminar Participante de Reserva
```yaml
DELETE /smartdine/api/reservations/{reservationId}/participants/{participantId}
```

**Prioridad:** 🔴 CRÍTICA
**Bloquea:** Gestión completa de participantes

**Propuesta de Especificación:**

```yaml
/smartdine/api/reservations/{reservationId}/participants/{participantId}:
  delete:
    tags:
      - Reservations
    summary: Remove a participant from a reservation
    description: |
      Removes a participant from an existing reservation.
      Validations:
      - Requester must be the owner of the reservation
      - The participant to remove must exist in the reservation
      - Cannot remove the reservation owner (use cancel reservation instead)
    operationId: removeParticipantFromReservation
    security:
      - bearerAuth: []
    parameters:
      - in: path
        name: reservationId
        required: true
        schema:
          type: integer
          format: int64
        description: ID of the reservation
      - in: path
        name: participantId
        required: true
        schema:
          type: integer
          format: int64
        description: ID of the participant to remove
    responses:
      '204':
        description: Participant removed successfully
      '400':
        description: |
          Bad request - one of:
          - Cannot remove reservation owner
          - Participant not found in reservation
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
      '401':
        description: Unauthorized
      '403':
        description: |
          Forbidden - one of:
          - User is not a CUSTOMER
          - User is not the owner of the reservation
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
      '404':
        description: Reservation not found
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
```

**Validaciones a Implementar:**
```java
// En ReservationController (nuevo método)
@DeleteMapping("/reservations/{reservationId}/participants/{participantId}")
public ResponseEntity<Void> removeParticipant(
    @PathVariable Long reservationId,
    @PathVariable Long participantId,
    @AuthenticationPrincipal User user
) {
    // 1. Verificar autenticación y rol CUSTOMER
    // 2. Verificar que es el owner de la reserva
    // 3. Verificar que participantId no es el owner
    // 4. Eliminar ReservationParticipation
    // 5. Retornar 204 No Content
}
```

**Efecto:**
1. Eliminar la entidad `ReservationParticipation`
2. El participante deja de aparecer en la lista de participantes
3. Se libera un slot en la reserva

**Consideración:** También podría permitirse que un participante se elimine a sí mismo:
```yaml
DELETE /smartdine/api/reservations/{reservationId}/participants/me
```

---

## 📊 RESUMEN DE ENDPOINTS FALTANTES

### 🔴 Prioridad CRÍTICA (7 endpoints)

| # | Método | Endpoint | Funcionalidad Bloqueada |
|---|--------|----------|------------------------|
| 1 | GET | `/smartdine/api/users/search` | Búsqueda de usuarios |
| 2 | POST | `/smartdine/api/reservations/{id}/participants` | Añadir amigos a reservas |
| 3 | GET | `/smartdine/api/reservations/{id}/eligible-friends` | UX añadir participantes |
| 4 | POST | `/smartdine/api/communities/{id}/join-requests` | Solicitar unirse a comunidad |
| 5 | GET | `/smartdine/api/communities/{id}/join-requests` | Gestionar solicitudes (owner) |
| 6 | POST | `/smartdine/api/community-join-requests/{id}/accept` | Aceptar solicitudes |
| 7 | POST | `/smartdine/api/community-join-requests/{id}/reject` | Rechazar solicitudes |
| 8 | DELETE | `/smartdine/api/reservations/{id}/participants/{participantId}` | Eliminar participantes |

---

## 🛠️ PLAN DE IMPLEMENTACIÓN RECOMENDADO

### Sprint 1: Búsqueda y Participantes en Reservas (5-7 días)

**Objetivo:** Completar funcionalidades 1, 3 y 5

#### Semana 1 - Día 1-2: Búsqueda de Usuarios
- [ ] Crear endpoint `GET /users/search`
- [ ] Implementar `UserSearchResult` DTO
- [ ] Añadir query method en `CustomerRepository`
- [ ] Implementar lógica en `CustomerService`
- [ ] Tests unitarios e integración
- [ ] Documentar en api.yaml

#### Semana 1 - Día 3-5: Participantes en Reservas
- [ ] Crear endpoint `POST /reservations/{id}/participants`
- [ ] Crear `ReservationParticipationDTO`
- [ ] Implementar validaciones en `ReservationService`:
  - Verificar amistad
  - Validar capacidad
  - Detectar conflictos de tiempo
- [ ] Crear endpoint `DELETE /reservations/{id}/participants/{participantId}`
- [ ] Tests extensivos (casos edge)
- [ ] Documentar en api.yaml

#### Semana 1 - Día 6-7: Mejoras UX (Opcional)
- [ ] Crear endpoint `GET /reservations/{id}/eligible-friends`
- [ ] Implementar lógica de filtrado en `ReservationService`
- [ ] Tests y documentación

---

### Sprint 2: Solicitudes de Comunidad (4-6 días)

**Objetivo:** Completar funcionalidad 4

#### Semana 2 - Día 1-2: Modificaciones de Entidades
- [ ] Añadir `COMMUNITY_JOIN_REQUEST` a enum `RequestType`
- [ ] Añadir campo `community` en entidad `Request`
- [ ] Añadir campo `message` en entidad `Request`
- [ ] Migración de base de datos
- [ ] Actualizar `RequestDTO` para soportar comunidades

#### Semana 2 - Día 3-4: Endpoints de Solicitud
- [ ] Crear endpoint `POST /communities/{id}/join-requests`
- [ ] Implementar validaciones en `RequestService`:
  - Comunidad privada
  - No es miembro
  - No tiene petición pendiente
- [ ] Crear notificación para el owner
- [ ] Tests unitarios e integración
- [ ] Documentar en api.yaml

#### Semana 2 - Día 5-6: Gestión de Solicitudes
- [ ] Crear endpoint `GET /communities/{id}/join-requests`
- [ ] Implementar verificación de permisos (owner/admin)
- [ ] Crear endpoints `POST /community-join-requests/{id}/accept`
- [ ] Crear endpoints `POST /community-join-requests/{id}/reject`
- [ ] Implementar lógica de aceptación (crear Member)
- [ ] Tests de autorización
- [ ] Documentar en api.yaml

---

## 🧪 CASOS DE PRUEBA CRÍTICOS

### Test Suite 1: Búsqueda de Usuarios
```
✓ Buscar por nombre (case insensitive)
✓ Buscar por email
✓ No incluir usuario autenticado en resultados
✓ Marcar isFriend correctamente
✓ Marcar hasPendingRequest correctamente
✓ Paginación funciona correctamente
✗ Query muy corto (< 3 caracteres) retorna 400
✗ Usuario no autenticado retorna 401
✗ Usuario BUSINESS retorna 403
```

### Test Suite 2: Participantes en Reservas
```
✓ Owner puede añadir amigo como participante
✓ Participante aparece en lista de participantes
✗ No owner intenta añadir participante → 403
✗ Añadir no-amigo como participante → 400
✗ Añadir participante duplicado → 400
✗ Añadir cuando capacidad llena → 400
✗ Añadir con conflicto de tiempo → 400
✓ Owner puede eliminar participante
✗ No owner intenta eliminar participante → 403
✗ Intentar eliminar al owner → 400
```

### Test Suite 3: Solicitudes de Comunidad
```
✓ Usuario puede solicitar unirse a comunidad privada
✓ Se crea Request con tipo COMMUNITY_JOIN_REQUEST
✓ Se crea notificación para owner
✗ Solicitar unirse a comunidad pública → 400
✗ Solicitar cuando ya es miembro → 400
✗ Solicitar con petición pendiente → 400
✓ Owner puede ver solicitudes pendientes
✗ No-owner intenta ver solicitudes → 403
✓ Owner puede aceptar solicitud
✓ Usuario se añade como PARTICIPANT
✓ Owner puede rechazar solicitud
✗ Aceptar solicitud ya procesada → 400
```

---

## 📈 ESTIMACIONES Y RECURSOS

### Esfuerzo Total Estimado
```
Sprint 1 (Búsqueda + Participantes):  5-7 días
Sprint 2 (Comunidades):                4-6 días
Testing y Documentación:               2-3 días
Buffer (bugs, refactoring):            2 días
─────────────────────────────────────────────
TOTAL:                                13-18 días
```

### Recursos Necesarios
- **Backend Developer:** 1 persona a tiempo completo
- **QA Tester:** 0.5 personas (testing en paralelo)
- **Reviewer:** Revisiones de código (2-3 horas/día)

### Dependencias
- ✅ Sistema de autenticación JWT (implementado)
- ✅ Sistema de amistades (implementado)
- ✅ Sistema de reservas (implementado)
- ✅ Sistema de notificaciones (implementado)
- ⚠️ Base de datos actualizada (requiere migración para Request.community)

---

## 🔗 DEPENDENCIAS ENTRE ENDPOINTS

```
Funcionalidad 1: Búsqueda de Usuarios
├─ Independiente
└─ Requiere: Sistema de autenticación

Funcionalidad 3: Participantes en Reservas
├─ Requiere: GET /users/me/friends (✅ implementado)
├─ Requiere: Sistema de reservas (✅ implementado)
└─ Bloquea: Funcionalidad 4 (add participants desde community)

Funcionalidad 4: Solicitudes de Comunidad
├─ Requiere: Sistema de comunidades (✅ implementado)
├─ Requiere: Sistema de requests (✅ implementado base)
└─ Requiere modificación: Entidad Request

Funcionalidad 5: Eliminar Participantes
├─ Requiere: Funcionalidad 3 implementada primero
└─ Independiente de otras funcionalidades
```

---

## 📝 CAMBIOS EN API.YAML

### Archivos a Modificar

1. **api.yaml** (añadir 8 endpoints nuevos)
2. **Request.java** (añadir campos community y message)
3. **RequestType.java** (añadir COMMUNITY_JOIN_REQUEST)
4. **ReservationController.java** (añadir 3 métodos)
5. **FriendController.java** → Renombrar a **UserController.java** (añadir search)
6. **CommunityController.java** (añadir 3 métodos)

### Nuevos DTOs a Crear

```java
// 1. UserSearchResult.java
// 2. AddParticipantRequest.java
// 3. ReservationParticipationDTO.java
// 4. EligibleFriendDTO.java
// 5. CommunityJoinRequestDTO.java
```

### Nuevos Service Methods

```java
// CustomerService
Page<UserSearchResult> searchCustomers(String query, Long excludeId, Pageable pageable);

// ReservationService
ReservationParticipation addParticipant(Long reservationId, Long friendId, Long ownerId);
void removeParticipant(Long reservationId, Long participantId, Long ownerId);
List<EligibleFriend> getEligibleFriends(Long reservationId, Long ownerId);

// RequestService
Request createCommunityJoinRequest(Long communityId, Long userId, String message);
List<Request> getCommunityJoinRequests(Long communityId, RequestStatus status);
Request acceptCommunityJoinRequest(Long requestId, Long accepterId);
Request rejectCommunityJoinRequest(Long requestId, Long rejecterId);
```

---

## ⚠️ CONSIDERACIONES DE SEGURIDAD

### Authorization Checks Requeridos

| Endpoint | Verificación Requerida |
|----------|------------------------|
| `POST /reservations/{id}/participants` | user == reservation.owner |
| `DELETE /reservations/{id}/participants/{id}` | user == reservation.owner |
| `GET /reservations/{id}/eligible-friends` | user == reservation.owner |
| `POST /communities/{id}/join-requests` | user != member && community.visibility == PRIVATE |
| `GET /communities/{id}/join-requests` | user == owner OR user == admin |
| `POST /community-join-requests/{id}/accept` | user == owner OR user == admin |
| `POST /community-join-requests/{id}/reject` | user == owner OR user == admin |

### Validaciones de Integridad

1. **Evitar Race Conditions:**
   - Usar `@Transactional` en métodos de creación/eliminación
   - Verificar capacidad dentro de transacción

2. **Prevenir Abuse:**
   - Rate limiting en búsqueda de usuarios (max 30 req/min)
   - Limitar resultados de búsqueda (max 100 por página)

3. **Privacidad:**
   - No exponer emails en búsquedas (solo en resultados de amigos)
   - Sanitizar mensajes de join requests (max 500 caracteres)

---

## 📚 DOCUMENTACIÓN ADICIONAL REQUERIDA

### Para Desarrolladores Frontend/Móvil

1. **Guía de Integración:** Flujos completos con ejemplos de requests/responses
2. **Postman Collection:** Colección actualizada con nuevos endpoints
3. **Error Codes Reference:** Catálogo de códigos de error específicos
4. **Rate Limits:** Documentar límites por endpoint

### Para QA

1. **Test Cases:** Casos de prueba detallados por funcionalidad
2. **Edge Cases:** Escenarios límite y condiciones de error
3. **Performance Benchmarks:** Tiempos esperados de respuesta

---

## 🎯 CRITERIOS DE ACEPTACIÓN

### Funcionalidad 1: Búsqueda de Usuarios
- [x] Usuario puede buscar por nombre (parcial, case-insensitive)
- [x] Usuario puede buscar por email (parcial)
- [x] Resultados muestran si ya es amigo
- [x] Resultados muestran si hay petición pendiente
- [x] Paginación funciona correctamente
- [x] Excluye al usuario autenticado

### Funcionalidad 3: Participantes en Reservas
- [x] Owner puede añadir amigo como participante
- [x] Valida que sea amigo antes de añadir
- [x] Valida capacidad de la mesa
- [x] Valida conflictos de horario
- [x] No permite duplicados
- [x] Owner puede eliminar participantes
- [x] Participante puede verse a sí mismo en la lista

### Funcionalidad 4: Solicitudes de Comunidad
- [x] Usuario puede solicitar unirse a comunidad privada
- [x] Owner recibe notificación de solicitud
- [x] Owner puede ver solicitudes pendientes
- [x] Owner puede aceptar solicitud
- [x] Usuario aceptado se añade como PARTICIPANT
- [x] Owner puede rechazar solicitud
- [x] No permite solicitudes duplicadas

### Funcionalidad 5: Eliminar Participantes
- [x] Owner puede eliminar participante de reserva
- [x] No permite eliminar al owner
- [x] Valida permisos antes de eliminar

---

## 📞 CONTACTO Y SEGUIMIENTO

**Autor del Análisis:** Arquitectura de Software
**Fecha de Emisión:** 07/01/2026
**Próxima Revisión:** Después de Sprint 1

**Archivos Relacionados:**
- [api.yaml](../api.yaml)
- [FriendController.java](../src/main/java/com/smartDine/controllers/FriendController.java)
- [ReservationController.java](../src/main/java/com/smartDine/controllers/ReservationController.java)
- [Request.java](../src/main/java/com/smartDine/entity/Request.java)
- [Community.java](../src/main/java/com/smartDine/entity/Community.java)
- [Agents.md](../Agents.md)

---

**FIN DEL ANÁLISIS**

*Este documento debe actualizarse después de cada sprint de implementación para reflejar el progreso real.*
