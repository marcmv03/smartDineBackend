<!--
Guidance for AI coding agents working on the smartDineBackend repository.
Keep this short, actionable and specific to the codebase. Update when architecture
or major conventions change.
-->

# smartDineBackend — AI assistant instructions

This Spring Boot (Java 17) backend implements REST APIs for SmartDine. Use the
information below to make safe, repository-aware code changes.

Summary (big picture)
- Spring Boot application (entry: `com.smartDine.SmartDineApplication`).
- Typical layering: controllers -> services -> repositories -> JPA entities.
- **Controllers return DTOs, NOT entities**. All API responses use Data Transfer Objects.
- Security: JWT-based stateless auth via `configs.JwtAuthenticationFilter` and
  `configs.SecurityConfig`. Roles use the `entity.Role` enum and `entity.User`.
- Database: Spring Data JPA repositories (e.g. `repository.RestaurantRepository`).

Quick dev workflows
- Build and run tests: use Maven from project root. Example:
  - `./mvnw test` (Windows: run `mvnw.cmd test` or use `.\mvnw.cmd test` in PowerShell)
  - `./mvnw spring-boot:run` to start the app locally (Windows: `mvnw.cmd spring-boot:run`).
- Tests use H2 (in-memory) for unit/integration tests. Test properties are under
  `src/test/resources/application-test.properties`.

Project-specific conventions and patterns
- Controllers live under `com.smartDine.controllers` and return Spring
  `ResponseEntity<DTO>`. Example: `RestaurantController` enforces role checks using
  `@AuthenticationPrincipal User` and `User.getRole()`.
- **DTOs (Data Transfer Objects)** live under `com.smartDine.dto`. All DTOs must:
  - Have an `id` field with getters/setters
  - Implement `toEntity(DTO dto)` static method (checks `dto.getId() != null` before setting)
  - Implement `fromEntity(Entity entity)` static method (always sets id)
  - Implement `fromEntity(List<Entity>)` static method for list conversions
  - Use scalar values or arrays only; NO nested entity objects (use IDs instead)
- Services are thin layers that validate business rules and call repositories.
  Prefer adding validation to the service layer (see `RestaurantService` name-uniqueness checks).
  **Services must use `DTO.toEntity()` for entity construction**, not manual field assignment.
- Repositories extend `JpaRepository` and use Spring Data query methods,
  e.g. `findByNameContainingIgnoreCase` in `RestaurantRepository`.
- Entities use JPA annotations and Lombok for getters/setters in some classes.
  Inheritance: `User` is abstract with `@Inheritance(JOINED)` and concrete
  subclasses like `Business` (owner of restaurants).
- Security: JWT is parsed in `JwtAuthenticationFilter`; authentication is
  stateless. Public endpoints are configured in `SecurityConfig` (note: GET
  restaurants are permitted publicly). When adding new endpoints, ensure
  security rules are updated accordingly.

Integration points and external dependencies
- Postgres is used at runtime (dependency in `pom.xml`) but tests use H2.
- OAuth2 client and jjwt are present; `JwtService` handles token creation/validation.
- Dev: `spring-boot-devtools` is present for hot reload.

Style and safe-change rules for AI agents
- Preserve existing API contracts (paths under `/smartdine/api/` and response
  shapes). Backwards-incompatible changes must include migration notes and
  require explicit approval.
- Use service-layer validation (throw `IllegalArgumentException` as current
  pattern) rather than returning nulls.
- When editing entities or repositories, update tests under `src/test/java/...`
  and `src/test/resources/application-test.properties` to run with H2.
- Do not commit secrets or modify `application.properties` with real credentials.
- **Controllers must use `DTO.fromEntity()` to convert service responses** before
  returning `ResponseEntity<DTO>`. Use list version for collections.

Files to inspect for context when changing behavior
- `src/main/java/com/smartDine/configs/SecurityConfig.java` — security rules.
- `src/main/java/com/smartDine/configs/JwtAuthenticationFilter.java` — JWT parsing.
- `src/main/java/com/smartDine/controllers/RestaurantController.java` — example endpoint
  with role checks and DTO usage (`dto.RestaurantDTO`).
- `src/main/java/com/smartDine/services/RestaurantService.java` — service patterns and
  validation examples (name uniqueness, owner association).
- `src/main/java/com/smartDine/dto/RestaurantDTO.java` — example DTO with `toEntity()` and
  `fromEntity()` conversion methods.
- `pom.xml` and `HELP.md` — build and runtime hints (Postgres vs H2 tests).

Small examples (copyable patterns)
- Use service validation pattern (example taken from `RestaurantService`):
  - check repository for conflicting values with `findBy...`, throw
    `IllegalArgumentException` on conflict, then persist with `repository.save()`.
- Add endpoint security by checking `@AuthenticationPrincipal User user` and
  `user.getRole()` where necessary (see `RestaurantController.getRestaurantById`).
- Convert entity to DTO in controller: `RestaurantDTO.fromEntity(restaurant)` for single
  objects or `RestaurantDTO.fromEntity(restaurantList)` for collections.
- Convert DTO to entity in service: `Restaurant restaurant = RestaurantDTO.toEntity(dto);`

Known pitfalls and edge cases
- **Entity naming conflicts**: There is a `Table` entity class in `com.smartDine.entity`
  package. When using JPA's `@Table` annotation, always use fully qualified import
  `import jakarta.persistence.Table;` to avoid conflicts.
- **MenuItem hierarchy**: `MenuItem` is an abstract entity with `Dish` and `Drink` as
  subclasses using `@Inheritance(JOINED)`. When creating menu items, services must
  determine the subtype and call the appropriate repository.
- **DTO to Entity id handling**: When converting DTO to Entity using `toEntity()`, always
  check if `dto.getId() != null` before setting it on the entity, as JPA manages entity IDs.

If anything is ambiguous or you need more environment details (e.g. how CI
runs tests), ask the maintainers and include links to the files above in your
question.

End of file.

End of file.
