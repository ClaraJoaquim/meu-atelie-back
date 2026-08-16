# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

"Meu Ateliê" — Spring Boot 4.0.2 / Java 21 REST backend for a TCC (undergrad thesis) project. It's a multi-tenant SaaS-style backend where each `Usuario` (user) owns one `Loja` (store) and manages their own `Cliente`s (customers), `Produto`s, `Material`s, `Encomenda`s (orders), etc. The Angular frontend (default CORS origin `http://localhost:4200`) is a separate repo.

Note the package is `tcc.meu_atelie` (underscore), not `tcc.meu-atelie` (invalid Java identifier) — see `HELP.md`.

## Commands

This is a Maven project using the wrapper. Run from the repo root.

- Build: `./mvnw clean install` (`mvnw.cmd` on Windows cmd; the `Bash` tool's POSIX shell can call `./mvnw`)
- Run the app: `./mvnw spring-boot:run`
- Run all tests: `./mvnw test`
- Run a single test class: `./mvnw test -Dtest=MeuAtelieApplicationTests`
- Run a single test method: `./mvnw test -Dtest=ClassName#methodName`
- Package a jar: `./mvnw package`

### Database

Requires a local PostgreSQL instance. Connection is hardcoded in `src/main/resources/application.properties`:
- URL: `jdbc:postgresql://localhost:5432/TCC_Meu_Atelie`
- `spring.jpa.show-sql=true`, dialect `PostgreSQLDialect`
- No Flyway/Liquibase — schema comes from Hibernate entity mappings (JPA annotations on `models/*.java` are the source of truth for the DB schema)
- `JWT_SECRET` is also set here (not via env in dev); `TokenService` falls back to a hardcoded dev secret if the env var is absent or under 32 chars

## Architecture

Standard layered Spring MVC structure, one package per concern under `src/main/java/tcc/meu_atelie/`:

- `controller/` — `@RestController`s, thin. Extract the caller's email via `Authentication authentication` / `authentication.getName()` (the JWT subject) and pass it into the service layer for per-user scoping. No business logic here.
- `services/` — all business logic, ownership checks, and entity↔DTO mapping. Services are constructor- or field-injected (mixed style in the codebase; either is fine).
- `repositories/` — Spring Data JPA interfaces. Ownership-scoped queries follow the pattern `findByUsuarioEmail(...)`, `findByIdAndUsuarioEmail(id, email)`, `findByUsuarioEmailAndAtivoTrue(...)` — derived query methods based on the entity's relation to `Usuario`.
- `models/` — JPA `@Entity` classes (Lombok `@Getter`/`@Setter`). These define the actual DB schema.
- `dto/` — read-side response shapes returned to the frontend.
- `forms/` — write-side request bodies (`@RequestBody`) for create/update endpoints. Naming convention: incoming payloads are `*Form` (e.g. `ClienteForm`), outgoing payloads are `*DTO` (e.g. `ClienteResumoDTO`, `ClienteDetalheDTO`). Don't reuse entities directly as request/response bodies.
- `auth/` — JWT auth stack (see below).
- `enums/` — e.g. `StatusEncomenda`.

### Multi-tenancy / ownership model

There is no admin/roles system — authorization is purely "does this row belong to the logged-in user." Almost every entity that a user manages (`Cliente`, `Encomenda`, `Produto`, `Material`, ...) has a `@ManyToOne Usuario usuario` (or is reachable through one, e.g. `Endereco` → `Cliente` → `Usuario`). Every service method that reads/writes such an entity must scope the query by the authenticated user's email (via repository methods like `findByIdAndUsuarioEmail`), not just by ID — otherwise one user could access another user's data. Follow this pattern when adding new entities/endpoints.

`Loja` is 1:1 with `Usuario` and created together at signup (`UsuarioController.criarUsuario`).

### Auth flow (`auth/` package)

- `POST /usuarios` — signup: creates `Usuario` + its `Loja` in one call, password hashed via `PasswordEncoder` (BCrypt) in `UsuarioService.salvarUsuario`.
- `POST /usuarios/login` — `UsuarioService.autenticar` checks credentials, `TokenService.gerarToken` issues a JWT (24h expiry, subject = email, claim `idUsuario`).
- `JwtAuthFilter` (a `OncePerRequestFilter`) reads `Authorization: Bearer <token>`, validates it via `TokenService`, loads the `Usuario` by email, and sets a `UsernamePasswordAuthenticationToken` (no granted authorities — there's no role/permission system) in the `SecurityContext`.
- `SecurityConfig`: stateless sessions, CSRF disabled, CORS restricted to `http://localhost:4200`. **Note:** `authorizeHttpRequests` currently ends in `.anyRequest().permitAll()`, so the filter chain does not actually enforce authentication on any route — controllers rely on `Authentication` being present (non-null `authentication.getName()`) when they need the caller's identity, which only holds if a valid Bearer token was sent. Keep this in mind when adding endpoints that must be protected: permitAll does not currently gate anything.

### File uploads

`WebConfig` serves static files from the local `uploads/` directory at `/uploads/**`. Uploaded images (referenced by e.g. `Produto`) are stored on local disk, not in the DB or object storage.

### Error handling

No global `@ControllerAdvice`/exception handler currently exists. Services throw plain `RuntimeException`s (or `ObjectNotFoundException` in `UsuarioService`) with Portuguese messages; some controllers catch exceptions manually and map to HTTP status (see `UsuarioController.criarUsuario` catching `DataIntegrityViolationException` → 409). Follow whichever pattern the surrounding controller/service already uses rather than mixing styles within one file.

### Language convention

All domain code — entity/field/method names, DTO fields, exception messages — is in Portuguese (matches the project's Brazilian TCC context). Keep new code consistent with this.
