# SmartDine Backend

This project is the backend for the SmartDine application - an innovative platform that connects restaurants with communities of food enthusiasts interested in discovering and trying new dining experiences.

## 📋 Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Architecture](#architecture)
- [Testing](#testing)
- [Docker Deployment](#docker-deployment)

## 🎯 Overview

SmartDine Backend is a RESTful API built with Spring Boot that provides:
- Restaurant management for business owners
- Customer reservation system
- Menu management with dishes and drinks
- JWT-based authentication and authorization
- Table availability and booking system

## 🛠 Technology Stack

- **Framework**: Spring Boot 3.5.4
- **Language**: Java 17
- **Database**: PostgreSQL 17.6 (production) / H2 (testing)
- **ORM**: Hibernate / JPA
- **Security**: Spring Security + JWT
- **Build Tool**: Maven
- **Testing**: JUnit 5, Spring Boot Test
- **Containerization**: Docker & Docker Compose

## 📁 Project Structure

```
smartDineBackend/
├── src/
│   ├── main/
│   │   ├── java/com/smartDine/
│   │   │   ├── SmartDineApplication.java     # Application entry point
│   │   │   ├── adapters/                     # External service adapters
│   │   │   ├── configs/                      # Configuration classes
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── SecurityConfig.java       # Security & auth configuration
│   │   │   ├── controllers/                  # REST API endpoints
│   │   │   ├── dto/                          # Data Transfer Objects
│   │   │   ├── entity/                       # JPA entities (database models)
│   │   │   ├── handlers/                     # Global exception handlers
│   │   │   ├── repository/                   # Database repositories
│   │   │   └── services/                     # Business logic layer
│   │   └── resources/
│   │       ├── application.properties        # Main configuration
│   │       └── application-prod.properties   # Production configuration
│   └── test/                                 # Test suite with H2 database
├── api.yaml                                  # OpenAPI specification
├── compose.yaml                              # Docker Compose configuration
├── Dockerfile                                # Multi-stage Docker build
├── Agents.md                                 # AI agent development guide
└── README-DOCKER.md                          # Docker deployment guide
```

### 📦 Package Breakdown

#### `controllers/` - API Layer
**Purpose**: Handles HTTP requests and responses

Controllers are responsible for:
- Receiving HTTP requests from clients
- Validating input using `@Valid` annotations
- Delegating business logic to services
- Converting entities to DTOs before responding
- Applying security rules (role checks, authentication)

**Key Controllers**:
- `AuthenticationController` - User login, registration, and token management
- `RestaurantController` - Restaurant CRUD operations for business owners
- `MenuItemController` - Menu management (dishes and drinks)
- `ReservationController` - Customer reservation system
- `TimeSlotController` - Restaurant availability management
- `TableController` - Table management and availability queries

**Important Rule**: Controllers **MUST** return `ResponseEntity<DTO>`, never entities directly.

#### `dto/` - Data Transfer Objects
**Purpose**: Define the shape of API requests and responses

DTOs are lean objects that:
- Contain only primitive types, Strings, and IDs (never nested entities)
- Include validation annotations (`@NotNull`, `@Min`, `@Email`, etc.)
- Implement conversion methods:
  - `toEntity(DTO dto)` - Converts DTO to Entity
  - `fromEntity(Entity entity)` - Converts Entity to DTO
  - `fromEntity(List<Entity>)` - Bulk conversion

**Examples**:
- `RestaurantDTO` - Restaurant data (id, name, address, description)
- `ReservationDTO` - Reservation request/response
- `MenuItemDTO` - Abstract DTO with `DishDTO` and `DrinkDTO` subclasses
- `TimeSlotDTO` - Restaurant operating hours

**Why DTOs?**: They decouple the API from database structure, reduce payload size, and prevent circular reference issues.

#### `entity/` - Database Models
**Purpose**: JPA entities that map to database tables

Entities represent the database schema:
- Annotated with `@Entity`, `@Table`, `@Column`
- Define relationships (`@ManyToOne`, `@OneToMany`, `@ManyToMany`)
- Use `FetchType.LAZY` by default for performance
- **Never exposed directly** in API responses

**Key Entities**:
- `User` (abstract) → `Customer`, `Business` - User hierarchy with joined inheritance
- `Restaurant` - Restaurant information with relationships to menu, tables, and timeslots
- `MenuItem` (abstract) → `Dish`, `Drink` - Menu item hierarchy
- `Reservation` - Customer bookings with table, timeslot, and date
- `RestaurantTable` - Tables belonging to restaurants
- `TimeSlot` - Restaurant availability windows

**Important Note**: The `Table` entity uses fully qualified `@jakarta.persistence.Table` to avoid naming conflict with the entity class name.

#### `services/` - Business Logic Layer
**Purpose**: Core business logic, validation, and orchestration

Services handle:
- Business rule validation
- Coordinating multiple repository calls
- Transaction management with `@Transactional`
- Converting DTOs to entities and vice versa
- Permission checks (e.g., ownership verification)

**Key Services**:
- `AuthenticationService` - User authentication and JWT token generation
- `RestaurantService` - Restaurant business logic and ownership validation
- `MenuItemService` - Menu creation with dish/drink type detection
- `ReservationService` - Reservation creation with table assignment
- `RestaurantTableService` - Table availability calculation
- `TimeSlotService` - Timeslot management and conflict detection

**Transaction Pattern**: All methods that modify data use `@Transactional`, read-only methods use `@Transactional(readOnly = true)`.

#### `repository/` - Data Access Layer
**Purpose**: Database queries using Spring Data JPA

Repositories:
- Extend `JpaRepository<Entity, ID>`
- Provide automatic CRUD operations
- Define custom query methods (e.g., `findByRestaurantId`)
- Use `@Query` for complex JPQL queries
- Use `JOIN FETCH` to avoid N+1 query problems

**Examples**:
- `RestaurantRepository` - Queries for restaurant data
- `ReservationRepository` - Find reservations by customer, date, and timeslot
- `RestaurantTableRepository` - Table queries with eager loading

#### `configs/` - Application Configuration
**Purpose**: Security, authentication, and app-wide settings

**Key Classes**:
- `SecurityConfig` - Configures Spring Security, defines public/private endpoints
- `JwtAuthenticationFilter` - Intercepts requests, validates JWT tokens
- `JwtService` - JWT token creation and validation

**Security Flow**:
1. Client sends request with JWT in `Authorization: Bearer <token>` header
2. `JwtAuthenticationFilter` extracts and validates token
3. Authentication is set in Spring Security context
4. Controllers access user via `@AuthenticationPrincipal`

#### `handlers/` - Exception Handling
**Purpose**: Global exception handling for consistent error responses

Exception handlers catch:
- `IllegalArgumentException` - Business rule violations (400 Bad Request)
- `EntityNotFoundException` - Resource not found (404)
- Validation errors - Bean validation failures (400)
- Authentication errors - Invalid credentials (401/403)

#### `adapters/` - External Integrations
**Purpose**: Interfaces with third-party services

Currently contains adapters for external APIs or services (e.g., payment gateways, email services).

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 17.6 (or use Docker)

### Local Development

1. **Clone the repository**
```bash
git clone https://github.com/marcmv03/smartDineBackend.git
cd smartDineBackend
```

2. **Configure database**
   - Create PostgreSQL database named `smartDine`
   - Update `src/main/resources/application.properties` with your credentials

3. **Run the application**
```powershell
.\mvnw.cmd spring-boot:run
```

The API will be available at `http://localhost:8080`

### Running Tests
```powershell
# Run all tests
.\mvnw.cmd test

# Run specific test class
.\mvnw.cmd test -Dtest=ReservationServiceTest

# Run with coverage
.\mvnw.cmd test jacoco:report
```

Tests use an in-memory H2 database configured in `src/test/resources/application-test.properties`.

## 📚 API Documentation

The complete API specification is available in `api.yaml` (OpenAPI 3.0 format).

**API Base Path**: `/smartdine/api`

**Common Endpoints**:
- `POST /smartdine/api/auth/login` - User authentication
- `POST /smartdine/api/auth/register` - New user registration
- `GET /smartdine/api/restaurants` - List all restaurants (public)
- `POST /smartdine/api/restaurants` - Create restaurant (business only)
- `POST /smartdine/api/reservations` - Create reservation (customer only)
- `GET /smartdine/api/restaurants/{id}/tables/available` - Check table availability

## 🏗 Architecture

### Layered Architecture

```
Client Request
      ↓
[Controllers]  ← Handles HTTP, returns DTOs
      ↓
  [Services]   ← Business logic, validation
      ↓
[Repositories] ← Database access
      ↓
  [Database]   ← PostgreSQL
```

### Key Design Patterns

1. **DTO Pattern**: Separate API contracts from internal models
2. **Repository Pattern**: Abstract data access via Spring Data JPA
3. **Service Layer**: Centralize business logic and validation
4. **Dependency Injection**: Constructor-based injection throughout
5. **JWT Stateless Auth**: No server-side session storage

### Database Design Highlights

- **User Hierarchy**: `JOINED` inheritance strategy for User → Customer/Business
- **Menu Polymorphism**: `JOINED` inheritance for MenuItem → Dish/Drink
- **Lazy Loading**: Relationships use `LAZY` fetch by default
- **Cascading**: Careful cascade configuration to prevent accidental deletions

## 🧪 Testing

The test suite includes:
- **Unit Tests**: Service layer business logic
- **Integration Tests**: Controller + Service + Repository + H2 database
- **Test Coverage**: Aim for >80% coverage on service layer

**Testing Best Practices**:
- Use `@SpringBootTest` for integration tests
- Use `@ActiveProfiles("test")` to load test configuration
- Use `@Transactional` on test classes for automatic rollback
- Use `@DirtiesContext` to reset context between tests

## 🐳 Docker Deployment

### Quick Start with Docker Compose

```bash
# Start all services (PostgreSQL + Spring Boot)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

See [README-DOCKER.md](../README-DOCKER.md) for detailed Docker instructions.

### Building Docker Image

```bash
# Build image
docker build -t smartdine-backend .

# Run container
docker run -p 8080:8080 smartdine-backend
```

## 🔐 Security Notes

- JWT tokens expire after 1 hour (configurable via `security.jwt.expiration-time`)
- Passwords are hashed using BCrypt
- CORS is enabled for all origins (configure for production)
- Public endpoints: restaurant listing, authentication
- Protected endpoints require valid JWT token

## 📝 Development Guidelines

### Adding a New Feature

1. **Create Entity** (if needed) in `entity/` package
2. **Create DTO** in `dto/` with `toEntity()` and `fromEntity()` methods
3. **Create Repository** in `repository/` extending `JpaRepository`
4. **Create Service** in `services/` with `@Transactional` methods
5. **Create Controller** in `controllers/` returning `ResponseEntity<DTO>`
6. **Write Tests** in `src/test/java/` for service and controller
7. **Update `api.yaml`** with new endpoint documentation

### Code Conventions

- Use constructor injection for dependencies
- Services throw `IllegalArgumentException` for business rule violations
- Controllers perform role/ownership checks before delegating to services
- All DTOs must have `id`, `toEntity()`, `fromEntity()`, and list conversion methods
- Use Lombok only where already present
- Write clear, descriptive commit messages

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes following the conventions above
4. Write/update tests
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

## 📧 Contact

For questions or support, please open an issue on GitHub.

---

**Note**: See `Agents.md` for detailed guidelines when working with AI coding assistants on this project.