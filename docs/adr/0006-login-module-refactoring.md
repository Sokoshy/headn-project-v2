# ADR 0006: Login Module Refactoring — Spring Security 7 Patterns

## Status

Accepted

## Context

The login module was implemented during the initial security setup (ADR 0005) using patterns from Spring Security 5/6. With the migration to Spring Boot 4.0.4 / Spring Security 7, several patterns are now outdated or redundant:

1. Manual `CsrfTokenRequestAttributeHandler` with `setCsrfRequestAttributeName(null)` — unnecessary with Spring Security 7's native CSRF support
2. Custom `authenticationEntryPoint` checking `Accept` header — built-in `LoginUrlAuthenticationEntryPoint` handles this
3. Custom `accessDeniedHandler` returning raw 403 — built-in redirects to `/error` where `CustomErrorController` already handles 403
4. `CurrentAgentProvider` re-querying DB on every call — `@AuthenticationPrincipal` with custom `UserDetails` avoids this
5. Explicit `invalidateHttpSession(true)` and `deleteCookies("JSESSIONID")` on logout — defaults in Spring Security 7

## Decision

Refactor the login module to use idiomatic Spring Security 7 patterns.

### 1. CSRF — Native Pattern

Remove `CsrfTokenRequestAttributeHandler` and let Spring Security expose CSRF tokens automatically. Thymeleaf integrates natively with `th:field` for CSRF. The explicit `<input type="hidden" th:name="_csrf" th:value="${_csrf.token}">` remains in templates that don't use `th:field` (login page).

### 2. Authentication Entry Point — Built-in

Replace custom `authenticationEntryPoint` with Spring Security's default `LoginUrlAuthenticationEntryPoint`. It handles HTML redirect vs 401 automatically via content negotiation.

### 3. Access Denied Handler — Built-in

Remove custom `accessDeniedHandler`. Spring Security's default redirects to `/error` with status 403, where `CustomErrorController` renders the appropriate page.

### 4. Agent Principal — Custom UserDetails

Create `AgentPrincipal` implementing `UserDetails`, wrapping the `Agent` entity. `AgentDetailsService` returns `AgentPrincipal` instead of `User` Spring Security. Controllers use `@AuthenticationPrincipal AgentPrincipal` to access the agent directly without re-querying the DB.

### 5. Logout — Simplify

Remove explicit `invalidateHttpSession(true)` and `deleteCookies("JSESSIONID")` — these are defaults in Spring Security 7.

## Consequences

### Positive

- **Less code**: ~30 lines removed from SecurityConfig, ~15 lines from CurrentAgentProvider
- **More idiomatic**: Follows Spring Security 7 documented patterns
- **Better performance**: No DB re-query on every agent resolution
- **Strongly typed**: Controllers get `AgentPrincipal` with full `Agent` data
- **Easier maintenance**: Future Spring Security upgrades won't break custom patterns

### Negative

- **Migration effort**: All controllers using `CurrentAgentProvider` must be updated
- **New class**: `AgentPrincipal` adds one more class to maintain
- **Template change**: Login template CSRF input may need adjustment

### Mitigations

- Migration is mechanical (find/replace pattern)
- `AgentPrincipal` is a thin wrapper, minimal maintenance burden
- CSRF integration is well-documented in Spring Security 7

## Alternatives Considered

### Keep current patterns

- Simpler (no migration needed)
- Patterns work but are outdated
- Rejected because idiomatic patterns reduce maintenance burden and improve compatibility with future Spring Security versions

### Use @CurrentSecurityContext instead of @AuthenticationPrincipal

- More flexible (access full SecurityContext)
- More verbose in controllers
- Rejected because `@AuthenticationPrincipal` is more concise and sufficient for this use case
