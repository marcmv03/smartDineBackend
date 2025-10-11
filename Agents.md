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
- Security: `src/main/java/com/smartDine/configs/SecurityConfig.java` and `JwtAuthenticationFilter.java`.
- Tests: `src/test/java` and `src/test/resources/application-test.properties` (H2 in-memory DB).

Coding and change rules
- Preserve API paths under `/smartdine/api/` unless the user explicitly approves breaking changes.
- Prefer service-layer validation: throw `IllegalArgumentException` for input/business errors (this is the project's current pattern).
- When changing entities, repositories, or service signatures, update or add unit tests under `src/test/java` to cover happy path + one edge case.
- Use Spring Data query methods where appropriate (e.g., `findByNameContainingIgnoreCase`) rather than hand-rolled queries unless necessary.
- Use Lombok getters/setters only where already present; don't introduce Lombok in new files unless consistent with repository style.

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
- `compose.yaml` currently contains no services; do not assume Docker Compose dev services are available (see `HELP.md`).
- The `User` hierarchy uses `@Inheritance(JOINED)` and `Business` is a subclass — changes to user tables need careful migration considerations.
- Some controllers check role by calling `user.getRole()` and casting (`(Business) user`). Ensure type checks before casts to avoid ClassCastException.

If blocked or uncertain
- If a change requires infra (real Postgres, external OAuth client secrets, or CI settings), present a clear list of missing items and a fallback (modify tests to use H2 or mock external calls).
- Ask maintainers for approval for backward-incompatible API changes or database migrations.

Contact and further context
- See `.github/copilot-instructions.md` for a complementary, shorter guide focused on Copilot-style agents.
- For detailed architecture or big changes, request a human reviewer and include the list of files you propose to change.

End of Agents.md
