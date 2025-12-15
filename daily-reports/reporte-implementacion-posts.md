# Reporte de Implementación de Posts

## 1. Resumen Ejecutivo
Se implementó el módulo de publicaciones comunitarias para permitir que los miembros creen, consulten, actualicen y eliminen posts dentro de comunidades, respetando roles y visibilidad de cada comunidad.

## 2. Modelo de Datos y Persistencia
- **Entidad `CommunityPost`**: incluye `title`, `description`, marca de tiempo `publishedAt` y relaciones obligatorias con `Member` (autor) y `Community`, con validaciones de longitud y obligatoriedad. El sello de tiempo se asigna automáticamente antes de persistir. 
- **Repositorio `CommunityPostRepository`**: expone consultas paginadas por autor y por comunidad, además de búsquedas por título o descripción combinando filtros de autor/comunidad y texto.

## 3. DTOs y Mapeo
- **DTOs de salida**: `CommunityPostResponseDTO` y `CommunityPostSummaryDTO` comparten los campos comunes definidos en `CommunityPostBaseDTO` (id, título, fechas y referencias de comunidad/autor) y añaden únicamente los datos específicos necesarios en cada respuesta.
- **DTOs de entrada**: `CreateCommunityPostRequestDTO` y `UpdateCommunityPostRequestDTO` validan tamaño y obligatoriedad de título y descripción para evitar datos incompletos.

## 4. Lógica de Dominio y Autorización
- **Creación**: solo miembros con rol `ADMIN` u `OWNER` pueden crear posts. El servicio valida la existencia de comunidad y usuario, obtiene la membresía y persiste el post mappeado desde el DTO.
- **Lectura**: si la comunidad es pública se permite acceso libre; en comunidades privadas se exige usuario autenticado y pertenencia validada. Las búsquedas por autor o comunidad soportan filtros de texto opcionales y paginación.
- **Actualización/Eliminación**: permiten modificaciones al autor original o a miembros `ADMIN/OWNER`; de lo contrario se lanza una excepción de autorización.

## 5. API REST
- **Controlador `CommunityPostsController`**:
  - `POST /smartdine/api/community/posts`: crea un post para la comunidad indicada.
  - `GET /smartdine/api/community/posts/{postId}`: recupera un post individual respetando visibilidad.
  - `GET /smartdine/api/community/members/{memberId}/posts`: lista posts por autor con búsqueda opcional.
  - `PUT /smartdine/api/community/posts/{postId}` y `DELETE ...`: actualizan o eliminan posts según reglas de rol/autoria.
- **Controlador `CommunityController`**:
  - `GET /smartdine/api/communities/{communityId}/posts`: lista posts de una comunidad con paginación y filtro de texto, devolviendo resúmenes.

## 6. Validación y Pruebas
- **Pruebas de servicio**: verifican creación exitosa para administradores, rechazo de lectura de comunidades privadas sin membresía y bloqueo de actualizaciones cuando el actor no es autor ni administrador.
