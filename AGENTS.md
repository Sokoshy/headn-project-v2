# AGENTS.md — Gestionnaire de Bibliothèque

## Project Overview

A Library Management System (Gestionnaire de Bibliothèque) built with Spring Boot 4.0.4 / Java 25.
Migrated from legacy Java Servlet/JSP to a modern stack.

**Key technologies:** Java 25, Maven 3.9, Spring Boot 4.0.4, Spring MVC, Thymeleaf, Spring Data JPA, Spring Security, PostgreSQL 17, Flyway, Testcontainers.

**Architecture:** Classic layered — `controller` → `service` → `repository` → `entity`, with Thymeleaf templates and a custom CSS design system.

**Modules:** Accueil (Home), Livres (Books), Utilisateurs (Users), Emprunts (Loans), Agents (Staff), Audit (Audit Trail).

All phases of a 10-phase migration plan are completed. The project is production-ready with 260 passing tests.

**Available features:** Account system (Agent entity, login, LIBRARIAN/ADMIN roles, audit trail for loans). See `docs/account-system-plan.md` for details.

---

## Project Structure

```
.
├── pom.xml                    # Maven build (Spring Boot 4.0.4, Java 25)
├── mise.toml                  # Task runner aliases
├── docker-compose.yml         # PostgreSQL 17
├── init.sql                   # Seed data (8 books, 5 users, sample loans)
├── REFACTORING_PLAN.md        # Full migration plan & completion criteria
├── CONTEXT.md                 # Domain vocabulary and glossary
├── docs/                      # Phase deliverables (phase-0 through phase-9)
│   ├── adr/                   # Architecture Decision Records
│   └── account-system-plan.md # Account system implementation plan
│
├── src/main/java/com/bibliotheque/
│   ├── BibliothequeApplication.java    # @SpringBootApplication entry point
│   ├── config/                         # SecurityConfig (with login), AgentDetailsService, CurrentAgentProvider, FlywayConfig, ValidationUtil
│   ├── exception/                      # BusinessException, LivreNotFoundException, etc.
│   ├── model/                          # Livre, Utilisateur, Emprunt, Agent, AuditLoan (JPA entities)
│   ├── repository/                     # Spring Data JPA repositories (Livre, Utilisateur, Emprunt, Agent, AuditLoan)
│   ├── service/                        # Business logic (LivreService, UtilisateurService, EmpruntService, AgentService, AuditService)
│   └── web/                            # Controllers (Livre, Utilisateur, Emprunt, Agent, Setup, Audit), GlobalExceptionHandler, CustomErrorController, GlobalModelAttributes
│
├── src/main/resources/
│   ├── application.yml                 # All Spring configuration
│   ├── templates/                      # Thymeleaf views
│   │   ├── layout/main.html            # Global layout (header, nav, flash messages, footer)
│   │   ├── index.html                  # Dashboard with stats
│   │   ├── livres/                     # Books: liste, formulaire, detail
│   │   ├── utilisateurs/               # Users: liste, formulaire, detail
│   │   ├── emprunts/                   # Loans: liste, detail
│   │   ├── agents/                     # Agents: list, form, detail (admin only)
│   │   ├── audit/                      # Audit: list with filters
│   │   ├── login.html                  # Login page
│   │   ├── setup.html                  # First admin setup
│   │   └── error/                      # 401, 403, 404, 500
│   ├── static/css/styles.css           # Complete design system (~1600 lines)
│   └── db/migration/                   # Flyway migrations
│       ├── V1__initial_schema.sql
│       ├── V2__align_schema_with_jpa.sql
│       ├── V3__protect_loan_history_and_enforce_single_active_loan.sql
│       ├── V4__drop_disponible_column.sql
│       ├── V5__add_expected_return_date.sql
│       ├── V6__backfill_expected_return_date_and_make_required.sql
│       └── V7__add_agents_and_audit_trail.sql
│
└── src/test/java/com/bibliotheque/
    ├── config/                         # ValidationUtilTest
    ├── model/                          # LivreTest, UtilisateurTest, EmpruntTest
    ├── repository/                     # Integration tests with Testcontainers
    ├── service/                        # Unit + integration tests
    ├── support/                        # PostgresIntegrationTestBase
    └── web/                            # Controller tests with MockMvc
```

---

## Setup Commands

**Prerequisites:** Java 25, Maven 3.9, Docker (for PostgreSQL).

```bash
# Start PostgreSQL database
mise run db
# or: docker compose up -d

# Build the project (compile + test + package)
mise run build
# or: mvn clean package

# Run tests only
mise run test
# or: mvn test

# Start the development server (listens on localhost:8080/bibliotheque)
mise run dev
# or: mvn spring-boot:run

# Full test suite including integration tests
mise run test:all
# or: mvn verify

# Stop database
mise run db:stop
# or: docker compose down
```

---

## Development Workflow

### Starting fresh

```bash
# 1. Start the database
mise run db

# 2. Run the app — Flyway auto-applies migrations, init.sql seeds sample data
mise run dev
```

The app is served at `http://localhost:8080/bibliotheque/`.

### Database

- PostgreSQL 17 via Docker Compose (`docker-compose.yml`).
- Port `5432`, database `bibliotheque`, user `admin`, password `password123`.
- The container initializes via `init.sql` which seeds 8 books, 5 users, and sample loan records.
- Flyway manages incremental schema changes (`V1` through `V7`). Do not manually alter the schema; always add a new Flyway migration.
- `spring.jpa.hibernate.ddl-auto=validate` — Hibernate validates against Flyway-managed schema at startup.

### Important routes

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Dashboard with stats |
| GET | `/livres` | Book list with search |
| GET | `/livres/nouveau` | Create book form |
| GET | `/livres/{id}` | Book detail |
| GET | `/livres/{id}/edit` | Edit book form |
| POST | `/livres` | Create book |
| POST | `/livres/{id}` | Update book |
| POST | `/livres/{id}/delete` | Delete book |
| GET | `/utilisateurs` | User list with search |
| GET | `/utilisateurs/nouveau` | Create user form |
| GET | `/utilisateurs/{id}` | User detail |
| GET | `/utilisateurs/{id}/edit` | Edit user form |
| POST | `/utilisateurs` | Create user |
| POST | `/utilisateurs/{id}` | Update user |
| POST | `/utilisateurs/{id}/delete` | Delete user |
| GET | `/emprunts` | Loan list (active + history) |
| GET | `/emprunts/{id}` | Loan detail |
| POST | `/emprunts` | Create loan |
| POST | `/emprunts/{id}/date-retour-prevue` | Update expected return date |
| POST | `/emprunts/{id}/retour` | Return a book |
| GET | `/agents` | Agent list (admin only) |
| GET | `/agents/new` | Create agent form (admin only) |
| GET | `/agents/{id}` | Agent detail (admin only) |
| GET | `/agents/{id}/edit` | Edit agent form (admin only) |
| POST | `/agents` | Create agent (admin only) |
| POST | `/agents/{id}` | Update agent (admin only) |
| POST | `/agents/{id}/deactivate` | Deactivate agent (admin only) |
| POST | `/agents/{id}/reactivate` | Reactivate agent (admin only) |
| GET | `/setup` | First admin setup (only when no agents exist) |
| POST | `/setup` | Create first admin (only when no agents exist) |
| GET | `/audit` | Audit trail listing with filters |
| GET | `/login` | Login page (Spring Security) |
| POST | `/login` | Submit credentials (Spring Security) |
| POST | `/logout` | Log out (Spring Security) |

### Key conventions

- **No logic in controllers.** All business rules live in `@Service` classes.
- **All POST forms must include CSRF token:** `<input type="hidden" th:name="_csrf" th:value="${_csrf.token}">`.
- **Flash messages:** Use `RedirectAttributes.addFlashAttribute("success", ...)` and `addFlashAttribute("error", ...)`.
- **Routes use POST for destructive actions** — never GET for delete/update.
- **French UI labels**, code in English (Java identifiers, routes).

---

## Testing Instructions

### Running tests

```bash
# Quick unit tests
mise run test
# or: mvn test

# Full suite including integration tests (requires Docker)
mise run test:all
# or: mvn verify
```

### Test suites (260 tests total)

**Controllers** — `src/test/java/com/bibliotheque/web/`
- `HomeControllerTest` — dashboard page, stats model
- `LivreControllerTest` — CRUD, search, validation errors, delete with confirm
- `UtilisateurControllerTest` — CRUD, search, email uniqueness, delete
- `EmpruntControllerTest` — create loan, return, access denied on nonexistent
- `EmpruntControllerTemplateTest` — view rendering for loan detail page
- `AgentControllerTest` — CRUD with success/error branches, activate/deactivate
- `SetupControllerTest` — first admin setup, redirect when agents exist
- `AuditControllerTest` — audit trail listing with filters
- `CustomErrorControllerTest` — custom error pages
- `GlobalExceptionHandlerTest` — exception mapping to views

**Services** — `src/test/java/com/bibliotheque/service/`
- `LivreServiceTest` — creation, modification, deletion blocking, availability
- `UtilisateurServiceTest` — creation, email normalization/uniqueness, deletion blocking
- `EmpruntServiceTest` — loan creation, return, already-returned guard, duplicate guard
- `EmpruntServicePostgresTest` — integration with real PostgreSQL via Testcontainers
- `AgentServiceTest` — creation, password hashing, activation/deactivation
- `AuditServiceTest` — audit trail recording and querying
- `LoanActivityServiceTest` — loan activity/return workflow
- `LoanPreparationServiceTest` — loan preparation helpers

**Repositories** — `src/test/java/com/bibliotheque/repository/`
- `LivreRepositoryPostgresTest` — findDisponibles, search, DDL import
- `UtilisateurRepositoryPostgresTest` — findByEmail, search, DDL import
- `EmpruntRepositoryPostgresTest` — CRUD, active loans, history, relationship cascade
- `SchemaMigrationPostgresTest` — validates all 7 Flyway migrations apply cleanly

**Models** — `src/test/java/com/bibliotheque/model/`
- `LivreTest` — equals/hashCode
- `UtilisateurTest` — equals/hashCode, email normalization
- `EmpruntTest` — estEnCours, estEnRetard, getNombreJoursEmprunt

**Config** — `src/test/java/com/bibliotheque/config/`
- `ValidationUtilTest` — HTML sanitization order and edge cases
- `AgentDetailsServiceTest` — email normalization, inactive-agent rejection, authority construction
- `CurrentAgentProviderTest` — SecurityContext resolution, null auth, anonymous user, agent lookup

### Test patterns

- **Controller tests** use `MockMvcBuilders.standaloneSetup(controller)` with mocked services.
- **Service unit tests** use Mockito `@ExtendWith(MockitoExtension.class)` to mock repositories.
- **Integration tests** extend `PostgresIntegrationTestBase` (abstract, Testcontainers).
- **Repository tests** use `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` + Testcontainers.
- All integration tests clean tables before each test to maintain isolation (emprunts first, then livres/utilisateurs).

### Adding tests

- Mirror the existing test patterns for the layer you're testing.
- For new service methods: unit test the business rules, integration test for persistence.
- Always use `@DisplayName` for test methods (descriptive French or English).

---

## Code Style Guidelines

### Java

- **Java 25** with `maven.compiler.release=25`.
- Package: `com.bibliotheque`.
- Constructor injection for all `@Service`, `@Controller`, `@Repository` — no field injection.
- `@Transactional(readOnly = true)` on service classes, override with `@Transactional` on write methods.
- Exceptions extend `BusinessException` (RuntimeException) or `ResourceNotFoundException`.
- Validation: Jakarta `@Valid` + `@NotBlank`/`@Email`/`@Size` on entities, `BindingResult` in controllers.
- `@InitBinder` with `setAllowedFields` to prevent mass assignment.

### Thymeleaf templates

- Use the global layout: `th:replace="~{layout/main :: layout ('Title', ~{::content})}"`.
- `.html` suffix, `templates/` directory.
- Active navigation link via `th:classappend="${#strings.startsWith(currentPath, '/livres')} ? 'active'"`.
- Flash messages: handled in `layout/main.html` via `th:if="${success}"` / `th:if="${error}"`.
- CSRF: `<input type="hidden" th:name="_csrf" th:value="${_csrf.token}">` on every POST form.

### CSS

- All styles in `src/main/resources/static/css/styles.css`.
- CSS custom properties (design tokens) in `:root`.
- Mobile-first responsive via breakpoints at 576px/768px/992px/1200px.
- BEM-like class naming (`.card`, `.card-header`, `.card-body`, `.btn`, `.btn-primary`).

### Naming conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Entities | Singular, PascalCase | `Livre`, `Utilisateur`, `Emprunt` |
| Repositories | `{Entity}Repository` | `LivreRepository` |
| Services | `{Entity}Service` | `LivreService` |
| Controllers | `{Entity}Controller` | `LivreController` |
| Routes | `/plural/nouveau`, `/plural/{id}/edit`, `/plural/{id}/delete` | `/livres/nouveau` |
| Templates | `/{plural}/{action}.html` | `/livres/liste.html` |
| Tables in DB | Plural, snake_case | `livres`, `utilisateurs`, `emprunts` |

---

## Build and Deployment

### Build

```bash
mise run build
# Produces: target/bibliotheque-app-1.0-SNAPSHOT.jar
```

The Spring Boot Maven Plugin packages a fat JAR. No external container needed.

### Environment

- Configuration: `src/main/resources/application.yml`.
- Context path: `/bibliotheque` (all routes are under this prefix).
- Spring DevTools is not configured; restart manually after changes or use your IDE's hot swap.
- Logging: `com.bibliotheque: DEBUG`, others `INFO`.

### Production considerations

- Set `spring.jpa.hibernate.ddl-auto=validate` (already default).
- Set `thymeleaf.cache=true` for production.
- Change `server.error.include-message` and `include-binding-errors` to `never` in production (currently `always` for dev).
- Override datasource credentials via environment variables or external `application-prod.yml`.

---

## Security

- **Spring Security** is configured in `SecurityConfig.java`.
- **CSRF protection** active for all POST requests.
- **Mandatory login** — all routes require authentication.
- **Role-based authorization** — LIBRARIAN (all except agent management) and ADMIN (all).
- **Agent identification** — automatically captured from `SecurityContextHolder`.
- **Setup flow** — `/setup` page for first admin (blocked when agents exist).
- Custom error pages for 401 (unauthorized), 403 (forbidden), 404, 500.
- `GlobalExceptionHandler` catches all exceptions, logs appropriately, returns user-friendly views.
- `CustomErrorController` handles the `/error` path for unknown routes.
- The global `@ControllerAdvice` (`GlobalModelAttributes`) exposes `currentPath` to all templates for active nav highlighting.

---

## Important Rules for Agents

1. **Always check `AGENTS.md` first** when starting work on this project.
2. **Never edit `init.sql` directly** — it's only for initial Docker seed data. Schema changes go through Flyway migrations (`V8__*.sql`, etc.).
3. **Always add CSRF tokens** to new POST forms.
4. **Use `RedirectAttributes`** for user-facing messages, never raw `HttpSession`.
5. **Do not bypass the service layer** — controllers must never contain business logic or repository calls.
6. **Route naming:** use `/livres/nouveau`, `/livres/{id}/edit`, `/livres/{id}/delete` (French-inspired for create/edit/delete).
7. **Template naming:** `liste.html` for list views, `formulaire.html` for forms, `detail.html` for detail views.
8. **Before adding a new dependency to `pom.xml`**, check if the Spring Boot starter for it already covers the need.
9. **Flyway migrations must be backward-compatible** with existing data in any environment.
10. **Test your changes** — run `mise run test` or at least the relevant test class.
11. **Communication language:** discuss in French with the user. All output files (code, docs, comments, commit messages) must be in English.
12. **Atomic commits only** — one logical change per commit. Do not bundle unrelated changes together.
13. **Agent identification:** always extract the current agent from `SecurityContextHolder`, never pass agent ID from forms.
14. **Audit trail:** record audit entries for loan creation and return in the `audit_loans` table.
15. **Role-based access:** use `@PreAuthorize` or URL-based authorization to restrict admin-only routes.
16. **Setup flow:** `/setup` page is accessible only when no agents exist in the database.
17. **Agent management:** admin-only routes for creating, editing, and deactivating agents.
18. **Audit trail display:** show audit info per loan (created by X, returned by Y) and globally via `/audit` page with filters.
19. **Navigation updates:** add Agents (admin only) and Audit links to the main navigation in `layout/main.html`.
