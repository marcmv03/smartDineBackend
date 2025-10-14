# Agents.md — Rules for automated coding agents

Purpose
- This file gives concise, actionable rules for automated coding agents (AI assistants, copilot-style bots) working on the smartDineBackend repository. Follow these rules to make safe, useful, and repository-aware edits.

High-level contract
- Inputs: small-to-medium change requests, bug reports, or feature requests related to the Java Spring Boot backend located under `src/main/java/com/smartDine`.
- Output: minimal, well-tested code changes that follow project conventions and preserve API compatibility unless the change requester allows breaking changes.
- Error modes: If a requested change would break tests, CI, or require secrets/infra not present in the repo, report the blocker and propose a non-invasive alternative.

Quick facts (where to look)
- App entry: `com.smartDine.SmartDineApplication`.
- Controllers: `src/main/java/com/smartDine/controllers` (use `ResponseEntity<>`).
- Services: `src/main/java/com/smartDine/services` — perform business validation here.
- Repositories: `src/main/java/com/smartDine/repository` — extend `JpaRepository`.
- DTOs: `src/main/java/com/smartDine/dto` — data transfer objects for API requests/responses.
- Entities: `src/main/java/com/smartDine/entity` — JPA entities (use Lombok for getters/setters).
- Security: `src/main/java/com/smartDine/configs/SecurityConfig.java` and `JwtAuthenticationFilter.java`.
- Exception handling: `src/main/java/com/smartDine/handlers/GlobalExceptionHandler.java`.
- Tests: `src/test/java` and `src/test/resources/application-test.properties` (H2 in-memory DB).

Coding and change rules
- Preserve API paths under `/smartdine/api/` unless the user explicitly approves breaking changes.
- Prefer service-layer validation: throw `IllegalArgumentException` for input/business errors (this is the project's current pattern). The `GlobalExceptionHandler` handles these exceptions and returns appropriate HTTP responses.
- When changing entities, repositories, or service signatures, update or add unit tests under `src/test/java` to cover happy path + one edge case.
- Use Spring Data query methods where appropriate (e.g., `findByNameContainingIgnoreCase`) rather than hand-rolled queries unless necessary.
- Use Lombok getters/setters consistently:
  - Entities use Lombok (`@Getter`, `@Setter`, `@AllArgsConstructor`, `@NoArgsConstructor`) for concise code.
  - DTOs may use standard getters/setters or Lombok depending on context — follow existing patterns in the file.
  - Some entities also have fluent setter methods that return `this` for method chaining (e.g., `setName(String name)` returns `this`).

Security and secrets
- Never add real secrets or credential values to `application.properties` or to commits. If needed, add a placeholder and document required environment variables.
- Respect current JWT-based, stateless auth. New endpoints must be declared in `SecurityConfig` to avoid accidentally being public.
- Controllers use `@AuthenticationPrincipal User user` to access the authenticated user. Check `user.getRole()` for authorization.
- Be careful with type casting: `User` is abstract with subclasses `Business` and `Customer`. Always check role before casting (e.g., `if (user instanceof Business business)`).

DTO patterns and entity conversion
Controllers should accept DTOs for input and may return either entities or DTOs depending on the endpoint:
- Input DTOs: Use `@Valid @RequestBody <DTO>` for request bodies with validation annotations (`@NotBlank`, `@Min`, etc.).
- Output: Currently mixed — some endpoints return entities (e.g., `ResponseEntity<Restaurant>`), others may return DTOs. Follow existing patterns in the controller you're modifying.

Polymorphic DTOs (inheritance hierarchy):
- `MenuItemDTO` is an abstract base class with `@JsonTypeInfo` and `@JsonSubTypes` annotations.
- Concrete implementations: `DishDTO` and `DrinkDTO` extend `MenuItemDTO`.
- Jackson uses the `type` property (e.g., `"type": "DISH"` or `"type": "DRINK"`) to deserialize to the correct subclass.
- Example from `MenuItemDTO.java`:
  ```java
  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "type"
  )
  @JsonSubTypes({
      @JsonSubTypes.Type(value = DishDTO.class, name = "DISH"),
      @JsonSubTypes.Type(value = DrinkDTO.class, name = "DRINK")
  })
  public abstract class MenuItemDTO { ... }
  ```

Entity creation from DTOs (service layer pattern):
- Services accept DTOs and manually construct entities from them.
- Example pattern from `RestaurantService.createRestaurant`:
  ```java
  Restaurant restaurant = new Restaurant();
  restaurant.setName(restaurantDTO.getName());
  restaurant.setAddress(restaurantDTO.getAddress());
  restaurant.setDescription(restaurantDTO.getDescription());
  return restaurantRepository.save(restaurant);
  ```
- For polymorphic types, use `instanceof` checks (see `MenuItemService.createMenuItem`):
  ```java
  if (menuItemDTO instanceof DishDTO dishDTO) {
      return createDish(dishDTO);
  } else if (menuItemDTO instanceof DrinkDTO drinkDTO) {
      return createDrink(drinkDTO);
  }
  ```
- DTOs generally don't have static `toEntity()` or `fromEntity()` helper methods; conversion is done manually in services.

Cross-origin and API conventions:
- Controllers use `@CrossOrigin(origins = "*")` for CORS support.
- All API endpoints are under `/smartdine/api/` (e.g., `/smartdine/api/restaurants`, `/smartdine/api/auth`).

Exception handling:
- `GlobalExceptionHandler` (`@ControllerAdvice`) catches common exceptions and returns structured JSON responses:
  - `MethodArgumentNotValidException` → 400 with field-level validation errors.
  - `IllegalArgumentException` → 400 with custom message (business logic errors).
  - `BadCredentialsException` → 401 for authentication failures.
  - Generic `Exception` → 500 for unexpected errors.
- Services should throw `IllegalArgumentException` for business rule violations. Controllers generally don't need try-catch blocks.

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
- `compose.yaml` currently contains no services; do not assume Docker Compose dev services are available (see `HELP.md`).
- The `User` hierarchy uses `@Inheritance(JOINED)` and `Business` is a subclass — changes to user tables need careful migration considerations.
- Some controllers check role by calling `user.getRole()` and casting (`(Business) user`). Ensure type checks before casts to avoid ClassCastException. Prefer pattern matching with `instanceof`: `if (user instanceof Business business)`.
- Entity inheritance: Both `User` (with `Customer`, `Business` subclasses) and `MenuItem` (with `Dish`, `Drink` subclasses) use `@Inheritance(JOINED)` strategy.
- DTO inheritance: `MenuItemDTO` uses Jackson polymorphic type handling with `@JsonTypeInfo` — ensure `type` field is included in JSON requests.
- Fluent setters: Some entity methods return `this` for chaining (e.g., `drink.setName("Coffee").setPrice(3.50)`). Be consistent with existing entity patterns.
- List initialization: When adding items to entity relationships (e.g., `restaurant.getMenu().add(item)`), check for null and initialize if needed:
  ```java
  List<MenuItem> menu = restaurant.getMenu();
  if (menu == null) {
      menu = new ArrayList<>();
  }
  menu.add(menuItem);
  restaurant.setMenu(menu);
  ```

Concrete examples from the codebase
1. Controller endpoint accepting DTO input and returning entity (from `RestaurantController`):
   ```java
   @PostMapping
   public ResponseEntity<Restaurant> createRestaurant(@Valid @RequestBody RestaurantDTO restaurantDTO) {
       Restaurant createdRestaurant = restaurantService.createRestaurant(restaurantDTO);
       return ResponseEntity.status(HttpStatus.CREATED).body(createdRestaurant);
   }
   ```

2. Service layer DTO-to-entity conversion with validation (from `RestaurantService`):
   ```java
   public Restaurant createRestaurant(RestaurantDTO restaurantDTO) {
       // Validate uniqueness
       List<Restaurant> existing = restaurantRepository.findByNameContainingIgnoreCase(restaurantDTO.getName());
       if (!existing.isEmpty()) {
           boolean exactMatch = existing.stream()
               .anyMatch(r -> r.getName().equalsIgnoreCase(restaurantDTO.getName()));
           if (exactMatch) {
               throw new IllegalArgumentException("Ya existe un restaurante con ese nombre");
           }
       }
       // Create entity from DTO
       Restaurant restaurant = new Restaurant();
       restaurant.setName(restaurantDTO.getName());
       restaurant.setAddress(restaurantDTO.getAddress());
       restaurant.setDescription(restaurantDTO.getDescription());
       return restaurantRepository.save(restaurant);
   }
   ```

3. Polymorphic DTO handling (from `MenuItemService`):
   ```java
   public MenuItem createMenuItem(MenuItemDTO menuItemDTO) {
       if (menuItemDTO instanceof DishDTO dishDTO) {
           return createDish(dishDTO);
       } else if (menuItemDTO instanceof DrinkDTO drinkDTO) {
           return createDrink(drinkDTO);
       }
       throw new IllegalArgumentException("Unsupported menu item type");
   }
   ```

4. Authorization with role and type checking (from `MenuItemService`):
   ```java
   public MenuItem createMenuItemForRestaurant(Long restaurantId, MenuItemDTO menuItemDTO, User user) {
       if (user == null || user.getRole() != Role.ROLE_BUSINESS || !(user instanceof Business business)) {
           throw new IllegalArgumentException("Only business users can create menu items");
       }
       if (!restaurantService.isOwnerOfRestaurant(restaurantId, business)) {
           throw new IllegalArgumentException("You do not own this restaurant");
       }
       MenuItem menuItem = createMenuItem(menuItemDTO);
       restaurantService.addMenuItem(restaurantId, menuItem);
       return menuItem;
   }
   ```

5. Controller with authenticated user (from `MenuItemController`):
   ```java
   @PostMapping("/restaurants/{restaurantId}/menu-items")
   public ResponseEntity<?> createMenuItem(
       @PathVariable Long restaurantId, 
       @Valid @RequestBody MenuItemDTO menuItemDto, 
       @AuthenticationPrincipal User user) {
       MenuItem menuItem = menuItemService.createMenuItemForRestaurant(restaurantId, menuItemDto, user);
       return new ResponseEntity<>(menuItem, HttpStatus.CREATED);
   }
   ```

If blocked or uncertain
- If a change requires infra (real Postgres, external OAuth client secrets, or CI settings), present a clear list of missing items and a fallback (modify tests to use H2 or mock external calls).
- Ask maintainers for approval for backward-incompatible API changes or database migrations.

Contact and further context
- See `.github/copilot-instructions.md` for a complementary, shorter guide focused on Copilot-style agents.
- For detailed architecture or big changes, request a human reviewer and include the list of files you propose to change.

Key files for understanding patterns:
- Controllers: `RestaurantController.java`, `MenuItemController.java`, `AuthenticationController.java`
- Services: `RestaurantService.java`, `MenuItemService.java`, `AuthenticationService.java`
- DTOs: `RestaurantDTO.java`, `MenuItemDTO.java` (abstract), `DishDTO.java`, `DrinkDTO.java`
- Entities: `Restaurant.java`, `MenuItem.java` (abstract), `Dish.java`, `Drink.java`, `User.java` (abstract), `Business.java`, `Customer.java`
- Exception handling: `GlobalExceptionHandler.java`
- Security: `SecurityConfig.java`, `JwtAuthenticationFilter.java`, `JwtService.java`

End of Agents.md
