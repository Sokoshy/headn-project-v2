# PRD — Operational Loan Model

## Problem Statement

The Loans page currently assembles several operational datasets directly in the controller: active loans, loan history, overdue loans, available books, and users. This interface is too close to the current implementation and will become fragile as the module evolves: history filters, pagination, search by user or book, clearer separation between active and overdue loans, guided loan creation, and support for an expected return date.

The goal is to make the Loans module more evolvable and safer, while allowing the Home page and other screens to reuse loan activity data without depending directly on JPA entities or Thymeleaf-specific page assembly.

## Solution

Introduce an operational loan model based on activity-oriented objects, separated into three deep modules:

- **Loan Activity**: operational read module for active loans, overdue loans, loan history, filters, future pagination, and indicators reusable by the Home page.
- **Loan Preparation**: form-preparation module for creating a new loan, including available books, selectable users, and a default expected return date.
- **Loan Lifecycle**: command module preserving loan creation, return registration, availability enforcement, and critical concurrency rules.

Screens should no longer receive all entities needed by the Loans page directly. They should consume limited and explicit objects adapted to the displayed activity or to the prepared action.

## User Stories

1. As a librarian, I want to see active loans, so that I can know which books are currently borrowed.
2. As a librarian, I want to see overdue loans separately, so that I can prioritize follow-up actions.
3. As a librarian, I want to see loan history, so that I can understand past activity.
4. As a librarian, I want active loans and overdue loans to have clear statuses, so that I do not confuse normal activity with late returns.
5. As a librarian, I want the Loans screen to load from one operational activity model, so that the screen remains stable as the data grows.
6. As a librarian, I want to filter loan history, so that I can find a past loan faster.
7. As a librarian, I want to search loans by user, so that I can review a user's borrowing activity.
8. As a librarian, I want to search loans by book, so that I can review the history of a specific book.
9. As a librarian, I want history pagination to be possible, so that the page remains usable when there are many loans.
10. As a librarian, I want the Home screen to reuse loan activity indicators, so that the dashboard reflects the same operational truth as the Loans screen.
11. As a librarian, I want the Books screen to rely on the same availability meaning, so that availability is consistent across the application.
12. As a librarian, I want to prepare a new loan with only available books, so that I cannot intentionally select a borrowed book.
13. As a librarian, I want to select from valid users, so that creating a loan is guided and less error-prone.
14. As a librarian, I want a default expected return date, so that the expected return can be captured consistently.
15. As a librarian, I want to enter or adjust the expected return date, so that the expected return reflects the real agreement with the user.
16. As a librarian, I want the expected return date to be distinct from the actual return date, so that planned and actual returns are never confused.
17. As a librarian, I want to register the return of a loan, so that the book becomes available again.
18. As a librarian, I want clear business error messages when creating a loan fails, so that I can correct the action.
19. As a librarian, I want concurrent attempts to borrow the same book to be prevented, so that availability remains trustworthy.
20. As a maintainer, I want consultation logic separated from command logic, so that filters and pagination do not risk breaking creation or return rules.
21. As a maintainer, I want views to consume objects oriented around activity, so that Thymeleaf does not depend on internal JPA relationships.
22. As a maintainer, I want controller tests to cross small stable seams, so that changing internal assembly does not require rewriting many tests.
23. As a maintainer, I want the activity model to be reusable by Home, so that dashboard indicators do not duplicate Loans logic.
24. As a maintainer, I want availability to remain a shared business invariant, so that Books and Loans screens do not diverge.
25. As a future agent, I want modules to use project vocabulary, so that navigation and implementation decisions remain AI-navigable.

## Implementation Decisions

- Build a **Loan Activity** module dedicated to operational read use cases.
- The Loan Activity module returns activity-oriented objects rather than JPA entities.
- The Loan Activity module owns active loan views, overdue loan views, history views, future search criteria, and future pagination results.
- Build a **Loan Preparation** module dedicated to preparing the new loan form.
- The Loan Preparation module returns selectable book options, selectable user options, and a default expected return date.
- Keep the current command behavior in the **Loan Lifecycle** module before any renaming or deeper restructuring.
- The Loan Lifecycle module owns creation, return registration, availability enforcement, and business exception translation.
- Do not start by renaming the existing command module. First extract read and preparation behavior to reduce risk.
- Separate read concerns from command concerns even if the Loans controller initially calls two modules for the GET screen.
- Treat **expected return date** and **actual return date** as separate domain concepts.
- Do not expose broad JPA entity graphs to Thymeleaf for the Loans operational view.
- Keep destructive and mutating actions behind POST routes with CSRF protection.
- Preserve the rule that controllers contain no business logic and do not call repositories directly.
- Preserve the existing database invariant that a book can have at most one active loan.
- Any schema change for expected return date must be introduced through a backward-compatible Flyway migration.
- The first implementation slice should migrate the GET Loans screen to the activity and preparation modules without changing creation or return behavior.
- The second implementation slice should add expected return date support.
- Later slices may add filters, search, and pagination through the activity module interface.

## Testing Decisions

- Tests should verify external behavior at module seams, not private assembly details.
- Controller tests should verify that the Loans screen receives activity and preparation objects, not five unrelated model attributes.
- Loan Activity tests should verify active, overdue, and historical classification from representative loan data.
- Loan Activity tests should cover empty states and mixed active/history data.
- Loan Preparation tests should verify that only available books are offered.
- Loan Preparation tests should verify that users are exposed as selectable options.
- Loan Preparation tests should verify the default expected return date once that rule is defined.
- Loan Lifecycle tests should continue to cover creation, return, duplicate active loan protection, and already-returned protection.
- Integration tests should continue to validate the database constraint preventing more than one active loan for the same book.
- If expected return date requires schema changes, repository or integration tests should validate persistence and migration behavior.
- Existing test patterns should be reused: controller tests with MockMvc and mocked modules, module tests with Mockito where persistence is not the focus, and PostgreSQL/Testcontainers tests for database invariants.

## Out of Scope

- Authentication and authorization changes.
- A generic CRUD abstraction for Book and User forms.
- Full dashboard redesign.
- Full pagination implementation in the first slice.
- Full historical search implementation in the first slice.
- Renaming the existing command module before read/preparation extraction is complete.
- Replacing Thymeleaf or changing the global layout.
- Manual database changes outside Flyway migrations.

## Further Notes

The top architectural goal is to deepen the Loans modules: a smaller interface should provide more leverage, while implementation details such as JPA relationships, availability queries, overdue classification, and form option assembly remain local to their modules.

The recommended implementation order is:

1. Introduce Loan Activity objects and module.
2. Migrate GET Loans to use the activity module.
3. Introduce Loan Preparation objects and module.
4. Migrate form option loading to the preparation module.
5. Add expected return date through a Flyway migration and command changes.
6. Add filters, search, and pagination through the activity module interface.
