# Agents.md — Rules for automated coding agents

Purpose
- This file gives concise, actionable rules for automated coding agents (AI assistants, copilot-style bots) working on the smartDineBackend repository. Follow these rules to make safe, useful, and repository-aware edits.
- Context : Read Readme.md to have context of the project

High-level contract
- Inputs: small-to-medium change requests, bug reports, or feature requests related to the Java Spring Boot backend located under `src/main/java/com/smartDine`.
- Output: minimal, well-tested code changes that follow project conventions and preserve API compatibility unless the change requester allows breaking changes.
- Error modes: If a requested change would break tests, CI, or require secrets/infra not present in the repo, report the blocker and propose a non-invasive alternative.

Quick facts (where to look)
- App entry: `com.smartDine.SmartDineApplication`.
- Controllers: `src/main/java/com/smartDine/controllers` (use `ResponseEntity<DTO>`).
- DTOs: `src/main/java/com/smartDine/dto` — Data Transfer Objects for API requests/responses.
- Services: `src/main/java/com/smartDine/services` — perform business validation here.
- Repositories: `src/main/java/com/smartDine/repository` — extend `JpaRepository`.
- Entities: `src/main/java/com/smartDine/entity` — JPA entities (internal use only, not exposed in API).
- Security: `src/main/java/com/smartDine/configs/SecurityConfig.java` and `JwtAuthenticationFilter.java`.
- Tests: `src/test/java` and `src/test/resources/application-test.properties` (H2 in-memory DB).
- Api documentation : api.yaml
- **Adapters**: `src/main/java/com/smartDine/adapters` — Adapter pattern implementations (e.g., `ImageAdapter` interface with `ImageS3Adapter` for S3 storage).
- **Custom Exceptions**: `src/main/java/com/smartDine/exceptions` — Custom exception classes (e.g., `RelatedEntityException`).
- **Static files (images)**: AWS S3 bucket configured via `S3Service` and `S3Config`. See "AWS S3 Storage" section below.

AWS S3 Storage for Static Files
- **Purpose**: Store and serve static files (restaurant images, menu item photos) using AWS S3.
- **Configuration**:
  - `S3Config.java`: Configures AWS S3 client with credentials from environment variables.
  - `S3Service.java`: Provides methods to upload, retrieve, and get metadata of files in S3.
  - Environment variables (defined in `.env`):
    - `AWS_ACCESS_KEY_ID`: AWS access key for S3 authentication
    - `AWS_SECRET_ACCESS_KEY`: AWS secret key for S3 authentication
    - `AWS_REGION`: AWS region where the S3 bucket is located (default: us-east-1)
    - `AWS_S3_BUCKET`: Name of the S3 bucket (default: smartdine-s3-bucket)
- **Usage Pattern**:
  - Controllers receive `MultipartFile` uploads
  - Call `S3Service.uploadFile(file, keyName)` to store in S3
  - Returns public URL to the stored file
  - Use `S3Service.getFile(keyName)` to retrieve files as `InputStreamResource`
- **Security**:
  - Files are currently uploaded with `PublicRead` ACL (publicly accessible)
  - For production, consider using presigned URLs for private access control
  - **NEVER commit AWS credentials** to the repository
  - Always use environment variables for AWS configuration
- **Testing**:
  - `S3ServiceTest.java`: Unit tests using Mockito to mock AWS S3 client
  - Tests cover `uploadFile()`, `getFile()`, and `getMetadata()` methods
  - Tests validate proper S3 API interactions without requiring real AWS credentials
- **File Naming Convention**:
  - Format: `restaurants/{restaurantId}/images/{uuid}.{extension}`
  - Example: `restaurants/123/images/550e8400-e29b-41d4-a716-446655440000.jpg`
  - UUID prevents filename collisions
- **API Endpoint**:
  - `POST /api/restaurants/{id}/images` - Upload image, returns URL and metadata
  - Controller: `ImageController.java`
  - Response: `UploadResponse` with `keyName`, `url`, `contentType`, `size`
- **Error Handling**:
  - `IOException` thrown by upload/download methods
  - Global exception handler (`GlobalExceptionHandler`) catches and processes errors
  - Validate file is not null/empty before upload
- **Dependencies**:
  - `aws-java-sdk-s3` in `pom.xml` for AWS SDK
  - Spring Boot `MultipartFile` for file uploads 

Coding and change rules
- Preserve API paths under `/smartdine/api/` unless the user explicitly approves breaking changes.
- Prefer service-layer validation: throw `IllegalArgumentException` for input/business errors (this is the project's current pattern).
- When changing entities, repositories, or service signatures, update or add unit tests under `src/test/java` to cover happy path + one edge case.
- Use Spring Data query methods where appropriate (e.g., `findByNameContainingIgnoreCase`) rather than hand-rolled queries unless necessary.
- Use Lombok getters/setters only where already present; don't introduce Lombok in new files unless consistent with repository style.
- **Controllers MUST return DTOs, never entities**. All the fields of the DTO should be scalar values or arrays, but never other entities.
- The exceptions are handled by global exception handler, located in package com.smartdine.handlers

Dependency Injection Style (IMPORTANT)
- **Preferred pattern: Constructor injection** for new code (see `ReservationController`, `ReservationService` for examples)
- Constructor injection makes dependencies explicit, improves testability, and allows final fields
- **Example pattern** (preferred for new controllers/services):
  ```java
  @RestController
  public class ReservationController {
      private final ReservationService reservationService;
      private final CustomerService customerService;

      public ReservationController(ReservationService reservationService, CustomerService customerService) {
          this.reservationService = reservationService;
          this.customerService = customerService;
      }
  }
  ```
- **Existing `@Autowired` field injection is acceptable** in existing code (e.g., `RestaurantController`, `TableController`)
- When modifying existing classes, follow the existing style in that file for consistency
- For new classes, prefer constructor injection

Validation Annotations (DTOs)
- Use Jakarta Bean Validation annotations on DTO fields:
  - `@NotNull(message = "...")` — for required fields
  - `@NotBlank(message = "...")` — for required non-empty strings
  - `@Min(value = X, message = "...")` — for minimum numeric values
  - `@Size(max = X, message = "...")` — for string length constraints
- Controllers use `@Valid @RequestBody` to trigger validation
- Validation errors are handled by `GlobalExceptionHandler.handleValidationExceptions()` returning `ValidationErrorDTO`
- **Example** (from `RestaurantTableDTO`):
  ```java
  @NotNull(message = "Table number is required")
  @Min(value = 1, message = "Table number must be at least 1")
  private Integer number;
  ```

Authorization Patterns in Controllers
- Check `user == null` first, return `HttpStatus.UNAUTHORIZED` 
- Check role/type with `user.getRole()` or `instanceof`, return `HttpStatus.FORBIDDEN` or throw `BadCredentialsException`
- For business-only endpoints, throw `BadCredentialsException` for clearer error messages:
  ```java
  if (!(user instanceof Business)) {
      throw new BadCredentialsException("Only business owners can perform this action");
  }
  ```
- Cast safely after instanceof check: `Business business = (Business) user;`
- Delegate ownership validation to service layer: `restaurantService.isOwnerOfRestaurant(id, business)`

DTO and Entity Conversion Rules (CRITICAL)
- **All DTOs must have an `id` field** with getters and setters.
- **All DTOs must implement two static methods**:
  1. `toEntity(DTO dto)`: Converts DTO to Entity. **MUST check if `dto.getId() != null` before assigning id to entity** (id is managed by JPA).
  2. `fromEntity(Entity entity)`: Converts Entity to DTO. Always sets the id from entity to DTO.
- **All DTOs must implement a list conversion method**: `fromEntity(List<Entity> entities)` that returns `List<DTO>` using streams.
- **Services must use `DTO.toEntity()` for entity construction**, not manual field-by-field assignment.
- **Controllers must use `DTO.fromEntity()` to convert service responses** before returning `ResponseEntity<DTO>`.
- **Example pattern** (see `RestaurantDTO`, `TimeSlotDTO`, `DishDTO`, `DrinkDTO`):
  ```java
  public static Restaurant toEntity(RestaurantDTO dto) {
      Restaurant entity = new Restaurant();
      if (dto.getId() != null) {
          entity.setId(dto.getId()); // Only if entity allows id setting
      }
      entity.setName(dto.getName());
      // ... other fields
      return entity;
  }
  
  public static RestaurantDTO fromEntity(Restaurant entity) {
      RestaurantDTO dto = new RestaurantDTO();
      dto.setId(entity.getId());
      dto.setName(entity.getName());
      // ... other fields
      return dto;
  }
  
  public static List<RestaurantDTO> fromEntity(List<Restaurant> entities) {
      return entities.stream()
          .map(RestaurantDTO::fromEntity)
          .collect(Collectors.toList());
  }
  ```
- **For polymorphic DTOs** (like `MenuItemDTO` with `DishDTO` and `DrinkDTO`):
  - Parent DTO should have abstract conversion handling.
  - Each subclass implements its own `toEntity()` and `fromEntity()` methods.
  - Parent DTO implements list conversion that checks instanceof for proper DTO type.
- **DO NOT expose entity relationships in DTOs**. Use IDs instead (e.g., `restaurantId` instead of `Restaurant` object).

Security and secrets
- Never add real secrets or credential values to `application.properties` or to commits. If needed, add a placeholder and document required environment variables.
- Respect current JWT-based, stateless auth. New endpoints must be declared in `SecurityConfig` to avoid accidentally being public.

Build, test, and run
- Use the Maven wrapper at project root. Windows PowerShell commands:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

- Tests run with H2 (test scope). If a change touches DB schema, update tests and `src/test/resources/application-test.properties` as necessary.

Pull request and commit behavior
- Make minimal, focused commits. Use clear commit messages describing the change and files modified.
- If a change touches public API shapes (controllers, DTOs, response bodies), document the change and add tests that assert the new behavior.

How to validate edits
- Run `mvnw.cmd test` locally (PowerShell) after edits. Aim for green tests. If tests fail for unrelated reasons, report test output and proposed next steps.

Edge cases and pitfalls known in this repo
- `compose.yaml` currently contains no services; do not assume Docker Compose dev services are available (see `Readme.md`).
- The `User` hierarchy uses `@Inheritance(JOINED)` and `Business` is a subclass — changes to user tables need careful migration considerations.
- Some controllers check role by calling `user.getRole()` and casting (`(Business) user`). Ensure type checks before casts to avoid ClassCastException.
- **MenuItem hierarchy**: `MenuItem` is an abstract entity with `Dish` and `Drink` as subclasses using `@Inheritance(JOINED)`. When creating menu items, services must determine the subtype and call the appropriate repository.
- **TimeSlot validation**: `TimeSlotDTO` has a custom `@AssertTrue` validation for `isValidTimeRange()` to ensure startTime < endTime.
- **Restaurant ownership**: Most restaurant operations require checking if the authenticated `Business` user is the owner via `RestaurantService.isOwnerOfRestaurant()`.
- **DTO to Entity id handling**: When converting DTO to Entity using `toEntity()`, always check if `dto.getId() != null` before setting it on the entity, as JPA manages entity IDs. Some entities may not expose `setId()` directly.
- **AWS S3 Configuration**: S3 credentials are loaded from environment variables (`.env` file). If `S3Service` fails to connect, verify:
  - Environment variables are set: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION`, `AWS_S3_BUCKET`
  - S3 bucket exists and has proper IAM permissions for the provided credentials
  - For local testing, mock `AmazonS3` client in tests (see `S3ServiceTest.java`)
- **File Upload Size Limits**: Spring Boot default max file size is 1MB. To increase, add to `application.properties`:
  ```properties
  spring.servlet.multipart.max-file-size=10MB
  spring.servlet.multipart.max-request-size=10MB
  ```

Entity and DTO Structure Guide
- **Restaurant**: 
  - Entity has: `id`, `name`, `address`, `description`, `imageUrl`, `owner` (Business), `menu` (List<MenuItem>), `timeSlots` (List<TimeSlot>), `tables` (List<RestaurantTable>)
  - DTO has: `id`, `name`, `address`, `description`, `imageUrl` (no nested objects)
- **MenuItem** (abstract):
  - Entity has: `id`, `name`, `description`, `price`
  - DTO has: `id`, `name`, `description`, `price`, `imageUrl`, `itemType`
  - Subclasses: `Dish`/`DishDTO` (with `courseType`, `elements`), `Drink`/`DrinkDTO` (with `drinkType`)
- **TimeSlot**:
  - Entity has: `id`, `startTime`, `endTime`, `dayOfWeek`, `restaurant` (Restaurant)
  - DTO has: `id`, `startTime`, `endTime`, `dayOfWeek`, `restaurantId` (Long, not Restaurant object)
- **RestaurantTable**:
  - Entity has: `id`, `number`, `capacity`, `outside`, `restaurant` (Restaurant)
  - DTO has: `id`, `number`, `capacity`, `outside`, `restaurantId` (Long)
- **Reservation**:
  - Entity has: `id`, `customer` (Customer), `restaurant` (Restaurant), `timeSlot` (TimeSlot), `restaurantTable` (RestaurantTable), `date`, `numGuests`, `createdAt`, `status`
  - DTO has: `id`, `timeSlotId`, `restaurantId`, `tableId`, `customerId`, `numCustomers`, `date`
  - **Multiple DTOs exist**: `ReservationDTO` (for creation), `ReservationDetailsDTO` (for customer view), `RestaurantReservationDTO` (for business view)
- **Customer**:
  - Entity extends `User`: `id`, `name`, `email`, `password`, `phoneNumber`
  - DTO has: `id`, `name`, `email`, `password`, `phoneNumber` (as String)

Service Layer Patterns
- **RestaurantService**: Validates name uniqueness, uses `RestaurantDTO.toEntity()` for creation.
- **BusinessService**: Creates restaurants for business owners, validates email/phone uniqueness.
- **MenuItemService**: Determines subtype (Dish/Drink) and calls `DishDTO.toEntity()` or `DrinkDTO.toEntity()`.
- **TimeSlotService**: Uses `TimeSlotDTO.toEntity()`, validates ownership and time slot conflicts.
- **ReservationService**: Assigns available tables, validates time slot availability and capacity.
- **TableService**: Creates tables for restaurants, validates ownership and unique table numbers per restaurant.
- **S3Service**: Manages file uploads/downloads to AWS S3. Methods throw `IOException` that must be handled by controllers or caught by `GlobalExceptionHandler`.
- **Pattern**: Services receive DTOs from controllers, convert to entities, perform validation, save, and return entities (controllers then convert back to DTOs).

Transaction Management (CRITICAL)
- **All service methods that modify database state MUST be annotated with `@Transactional`**
  - Create, update, delete operations MUST have `@Transactional`
  - Read-only operations SHOULD have `@Transactional(readOnly = true)` for optimization
- **Why this is critical**:
  - Ensures Hibernate session remains active during entity persistence
  - Properly manages JPA relationships (`@ManyToOne`, `@OneToMany`) 
  - Prevents `LazyInitializationException` when accessing lazy-loaded collections
  - Ensures database consistency with automatic rollback on exceptions
- **Example pattern**:
  ```java
  @Service
  public class YourService {
      
      @Transactional  // Required for CREATE/UPDATE/DELETE
      public Entity createEntity(DTO dto) {
          Entity entity = DTO.toEntity(dto);
          entity.setRelatedEntity(relatedEntity); // JPA manages this properly in transaction
          return repository.save(entity);
      }
      
      @Transactional(readOnly = true)  // Recommended for READ operations
      public Entity getEntityById(Long id) {
          return repository.findById(id)
              .orElseThrow(() -> new IllegalArgumentException("Entity not found"));
      }
  }
  ```
- **Common pitfall**: Tests may pass without `@Transactional` on service methods if the test class itself has `@Transactional`, but the application will fail in production. Always add `@Transactional` to service methods that modify state.

If blocked or uncertain
- If a change requires infra (real Postgres, external OAuth client secrets, or CI settings), present a clear list of missing items and a fallback (modify tests to use H2 or mock external calls).
- Ask maintainers for approval for backward-incompatible API changes or database migrations.

Contact and further context
- See `.github/copilot-instructions.md` for a complementary, shorter guide focused on Copilot-style agents.
- For detailed architecture or big changes, request a human reviewer and include the list of files you propose to change.

End of Agents.md
