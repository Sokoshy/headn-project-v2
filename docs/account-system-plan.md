# Account System Implementation Plan

## Overview

Add an **Agent** (staff) system with authentication, role-based authorization, and audit trail for loans.

## Key Decisions

See ADRs:
- [ADR 0004: Audit Trail via Separate Table](./adr/0004-audit-trail-separate-table.md)
- [ADR 0005: Authentication and Authorization Model](./adr/0005-authentication-and-authorization.md)

## New Entities

### Agent (staff/librarian)

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | Long | auto |
| `nom` | String | required |
| `email` | String | required, unique |
| `mot_de_passe` | String | hashed (bcrypt) |
| `role` | Enum | `LIBRARIAN`, `ADMIN` |
| `telephone` | String | nullable |
| `actif` | Boolean | default `true` |
| `date_creation` | LocalDateTime | auto |

### AuditLoan (audit trail)

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | Long | auto |
| `loan_id` | Long | FK → loans |
| `agent_id` | Long | FK → agents |
| `action` | Enum | `CREATION`, `RETURN` |
| `date_action` | LocalDateTime | auto |

## Implementation Phases

### Phase 1: Database Schema (Flyway V4)

- Create `agents` table
- Create `audit_loans` table
- Seed first admin account (or handle via setup page)

### Phase 2: Agent Entity & Repository

- Create `Agent` entity with JPA annotations
- Create `Role` enum (`LIBRARIAN`, `ADMIN`)
- Create `AuditAction` enum (`CREATION`, `RETURN`)
- Create `AuditLoan` entity
- Create `AgentRepository` with relevant queries
- Create `AuditLoanRepository`

### Phase 3: Service Layer

- Create `AgentService` (CRUD, password hashing, activation/deactivation)
- Create `AuditService` (record actions, query audit trail)
- Update `LoanService.create()` to record audit
- Update `LoanService.returnBook()` to record audit

### Phase 4: Spring Security Configuration

- Configure `SecurityFilterChain` for mandatory login
- Create `AgentDetailsService` (implements `UserDetailsService`)
- Configure form login with custom login page
- Configure role-based authorization (`@PreAuthorize` or URL-based)
- Configure password encoder (BCrypt)

### Phase 5: Controllers

- Create `AgentController` (admin only)
  - `GET /agents` - List all agents
  - `GET /agents/new` - Create form
  - `POST /agents` - Create agent
  - `GET /agents/{id}/edit` - Edit form
  - `POST /agents/{id}` - Update agent
  - `POST /agents/{id}/deactivate` - Deactivate agent
  - `POST /agents/{id}/reactivate` - Reactivate agent

- Create `SetupController`
  - `GET /setup` - Setup page (only if no agents exist)
  - `POST /setup` - Create first admin

- Create `AuditController`
  - `GET /audit` - Global audit journal with filters

- Update `LoanController`
  - Inject current agent from SecurityContext
  - Pass agent ID to service methods

### Phase 6: Templates

- Create `login.html` - Login page
- Create `setup.html` - First admin setup
- Create `agents/list.html` - Agent list
- Create `agents/form.html` - Agent create/edit form
- Create `agents/detail.html` - Agent detail
- Create `audit/list.html` - Audit journal
- Update `loans/detail.html` - Show audit info
- Update `layout/main.html` - Add Agents + Audit to navigation

### Phase 7: Tests

- Unit tests for `AgentService`
- Unit tests for `AuditService`
- Integration tests for `AgentRepository`
- Integration tests for `AuditLoanRepository`
- Controller tests for `AgentController`
- Controller tests for `SetupController`
- Controller tests for `AuditController`
- Update `LoanService` tests to verify audit recording
- Update `LoanController` tests to verify agent identification

## Navigation Updates

**Before:**
```
Home | Books | Users | Loans
```

**After:**
```
Home | Books | Users | Loans | Agents (admin only) | Audit
```

## Security Rules

1. All routes require authentication
2. `/setup` accessible only when no agents exist
3. Agent management (`/agents/*`) requires ADMIN role
4. Agent identification is automatic from SecurityContext
5. Passwords are hashed with BCrypt
6. CSRF protection enabled for all forms

## Audit Trail Display

### Per Loan

In loan detail page, show:
- "Created by [Agent] on [date]"
- "Returned by [Agent] on [date]" (if returned)

### Global Journal

Page `/audit` with filters:
- Filter by agent
- Filter by date range
- Filter by action type (CREATION, RETURN)
- Pagination
