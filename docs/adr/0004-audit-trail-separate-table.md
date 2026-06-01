# ADR 0004: Audit Trail via Separate Table

## Status

Accepted

## Context

We need to track which **Agent** validated each **Loan** creation and return. This is for accountability and traceability.

Two approaches were considered:

1. **Direct columns on `loans`** (`agent_creation_id`, `agent_return_id`)
2. **Separate `audit_loans` table** with `agent_id`, `action`, `date_action`

## Decision

We chose the **separate `audit_loans` table** approach.

## Consequences

### Positive

- **Extensible**: Adding new action types (e.g., `DATE_CORRECTION`) requires no schema changes, just new enum values
- **Query flexibility**: Easy to query "all actions by agent X" or "all actions today"
- **Clean separation**: Audit concerns don't pollute the core `loans` table
- **Future-proof**: If we need to track actions on other entities (books, users), the pattern is already established

### Negative

- **Slightly more complex**: Requires a JOIN to display audit info on loan detail pages
- **More tables**: One additional table to maintain

### Mitigations

- The JOIN is simple and indexed (`loan_id`)
- The table structure is straightforward with clear foreign keys

## Alternatives Considered

### Direct columns (`agent_creation_id`, `agent_return_id`)

- Simpler for 2 fixed actions
- Harder to extend if we add more action types
- Less flexible for querying audit data

Rejected because extensibility and query flexibility outweighed simplicity for this use case.
