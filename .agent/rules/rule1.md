---
trigger: always_on
---

# Programming rules

## General principles
- Follow SOLID, keep classes small and cohesive, avoid “god services”.
- Prefer composition over inheritance.
- Keep business rules in the service/domain layer, never in controllers.
- Use meaningful names, no abbreviations, consistent package structure.

## Backend architecture (Spring Boot)
- **Controller layer**: HTTP-only concerns (routing, request/response, status codes, validation triggering). No business logic.
- **Service layer**: business logic, orchestration, authorization checks (when not handled globally), transactions.
- **Repository layer**: persistence only (Spring Data JPA). No business rules.
- **Mapping boundary**: map DTOs ↔ domain/entities at the edge (controller/service boundary). Do not expose JPA entities in API responses.

## DTOs, validation, and mapping
- All public endpoints must use **RequestDTO** and **ResponseDTO**.
- Validate input with `jakarta.validation` annotations (`@NotNull`, `@Size`, etc.) and `@Valid` in controllers.
- Use explicit mappers (manual or MapStruct), keep mapping logic out of controllers.

## Transactions
- Mark service methods as `@Transactional` when they:
  - create/update/delete multiple entities,
  - require consistency across multiple repository calls,
  - depend on lazy-loaded relations that must remain open.
- Use `@Transactional(readOnly = true)` for read queries that do not modify state.
- Avoid transactions in controllers.

## Error handling
- Centralize errors with `@RestControllerAdvice`.
- Return consistent error payloads (timestamp, status, errorCode, message, path, validationErrors[]).
- Use specific exceptions (e.g., `NotFoundException`, `ConflictException`, `ForbiddenException`) and map them to correct HTTP statuses.

## Security
- Never trust client-provided user identifiers if the operation should be derived from the authenticated principal.
- Enforce authorization in service layer (or via method security) for sensitive operations.
- Do not log secrets, tokens, passwords, or PII.

## Persistence and performance
- Avoid N+1 queries; use fetch joins or entity graphs when needed.
- Prefer pagination for list endpoints.
- Add indexes in migrations for frequently filtered/sorted columns.

## API design
- Use RESTful resource naming, plural nouns, predictable status codes.
- Support pagination/sorting/filtering consistently (`page`, `size`, `sort`, plus feature-specific filters).
- Keep OpenAPI/Swagger updated with request/response schemas and error models.

## Logging and observability
- Use structured, leveled logs (INFO for lifecycle, DEBUG for diagnostics).
- Add correlation/request IDs if available; log key domain identifiers for traceability.

## Testing requirements
- Unit tests for services (business rules).
- Controller tests (MockMvc/WebTestClient) for validation, status codes, error contract.
- Integration tests with Testcontainers when persistence behavior matters.
- Tests must cover happy path + at least one failure path per endpoint.

## Code style
- Prefer immutability where possible.
- No unused code, no commented-out blocks.
- Keep methods short; extract helpers when logic grows.
- Document non-obvious decisions with short comments (why, not what).

# Conversation rules

## Context first (no guessing)
- Before implementing, ask for the minimum missing context needed (API contract, entities involved, auth rules, existing endpoints, naming conventions).
- If something is unclear, list assumptions explicitly and ask targeted questions to confirm.

## Plan before code
- Provide a short implementation plan (files to create/modify, steps, tests).
- Identify risks (breaking changes, migrations, backward compatibility) early.

## Precision and consistency
- Reuse existing project patterns (package structure, error model, response wrappers, naming).
- Avoid introducing new frameworks/patterns unless necessary; justify deviations.

## Communication cadence
- After receiving answers, restate the agreed scope in 3–6 bullets.
- When delivering, include:
  - what changed,
  - how to run tests,
  - any migrations/config updates required.

## Safety checks
- Never expose secrets in code or logs.
- Confirm behavior for edge cases (not found, conflicts, unauthorized, validation errors) before finalizing endpoints.