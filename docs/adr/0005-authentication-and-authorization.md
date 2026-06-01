# ADR 0005: Authentication and Authorization Model

## Status

Accepted

## Context

The library management system needs to track which **Agent** performs each action. Currently, all routes are public with no authentication. We need to add:

1. **Authentication**: Who is the current agent?
2. **Authorization**: What can this agent do?
3. **Audit trail**: Record the agent for each loan action

## Decision

We implement **mandatory login** with **role-based authorization**.

### Authentication

- **Spring Security** with form-based login
- Agents authenticate with email + password (bcrypt hashed)
- Session-based authentication (default Spring Security behavior)
- All routes require authentication (no public access)

### Authorization

Two roles with distinct permissions:

| Action | LIBRARIAN | ADMIN |
|--------|-----------|-------|
| Read operations | ✅ | ✅ |
| Loans (CRUD + return) | ✅ | ✅ |
| Books (CRUD) | ✅ | ✅ |
| Users (CRUD) | ✅ | ✅ |
| Agent management | ❌ | ✅ |
| Audit trail | ✅ | ✅ |

### Agent Identification

- Agent is identified **automatically** from `SecurityContextHolder`
- No manual agent selection in forms
- Agent ID is extracted from the authenticated session when creating/returning loans

### Setup Flow

- First agent (Admin) is created via `/setup` page
- `/setup` is accessible **only when no agents exist** in the database
- After setup, `/setup` redirects to `/login`
- Additional agents are created by Admins via `/agents/new`

## Consequences

### Positive

- **Security**: All routes protected, no unauthorized access
- **Auditability**: Agent identity automatically captured from session
- **Clear permissions**: Role-based access control is explicit and enforceable
- **User-friendly**: No manual agent selection required

### Negative

- **Login required**: Users must authenticate even for read-only operations
- **Password management**: Need to handle password hashing, reset, etc.
- **Session management**: Need to handle session timeout, concurrent sessions

### Mitigations

- Login page is simple and clear
- Password hashing is handled by Spring Security (bcrypt)
- Session timeout is configured in `application.yml`

## Alternatives Considered

### Manual agent selection (no login)

- Simpler to implement
- No real accountability (anyone can claim to be any agent)
- Rejected because it doesn't provide real security or audit trail

### Public read, authenticated write

- Less friction for users
- Audit trail only for write operations
- Rejected because it creates inconsistency and security gaps

### Separate auth service

- More scalable for large deployments
- Overkill for a library management system
- Rejected as premature optimization
