# ADR 0006: Custom UserDetails for Agent Authentication

## Status

Accepted

## Context

The authentication bridge between Spring Security and the Agent entity was using Spring Security's built-in `User` class. This meant the authenticated `Agent` object was not available in the `SecurityContext` — every request that needed the current agent had to do a database lookup via `AgentRepository.findByEmail()`.

This created a hidden performance cost: `CurrentAgentProvider.getCurrentAgent()` triggered a SQL query on every call, and it was called from multiple controllers and services per request.

## Decision

We introduce `AgentPrincipal`, a custom `UserDetails` implementation that wraps the `Agent` entity directly. `AgentDetailsService` now returns `AgentPrincipal` instead of `User`. `CurrentAgentProvider` extracts the `Agent` from the `SecurityContext` via `AgentPrincipal` without any database lookup.

## Consequences

### Positive

- **No redundant DB queries** — the Agent is resolved once at login and carried in the SecurityContext
- **Type-safe access** — callers get the full `Agent` entity, not just an email string
- **Single source of truth** — the SecurityContext is the authoritative source for the current agent

### Negative

- **Coupling to Spring Security** — `AgentPrincipal` implements `UserDetails`, tying the domain model to Spring Security's interface
- **Migration effort** — all usages of `CurrentAgentProvider` and any code that called `agentRepository.findByEmail(auth.getName())` must be updated

## Alternatives Considered

### Request-scoped cache in CurrentAgentProvider

Cache the Agent in a request-scoped bean, avoiding repeated DB lookups while keeping the standard `User` in the SecurityContext.

Rejected because it adds an extra layer of indirection and doesn't solve the fundamental problem: the Agent should be part of the authentication identity, not a side effect.

### Keep DB lookup, add second-level cache

Use Hibernate's second-level cache or Spring's `@Cacheable` to speed up the lookup.

Rejected because it's an optimization that masks the architectural issue. The Agent is already known at login time — there's no reason to re-derive it.
