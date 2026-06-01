# PRD — Loans Page UX Refactor: Filter Separation and Search Relocation

## Problem Statement

The Loans page (`/emprunts`) presents several usability issues that confuse librarians in daily use:

- **Filters in the wrong section.** Five status buttons (`Tous`, `Actifs`, `En retard`, `Terminés`, `Rendus en retard`) sit under the "Emprunts en cours" section header. Two of them — `Terminés` and `Rendus en retard` — can never match an active loan by definition, so clicking them empties the active section. The result of the click is then visible only in the history section further down the page, with no visible explanation of why the active section went blank.
- **Search field placed under the wrong section.** The "Rechercher un utilisateur / un livre" inputs are physically located inside the Historique section, but they actually filter both active loans and history. A librarian typing a name in what looks like the history search bar sees rows disappear in the active section above with no visible link between input and effect.
- **Ambiguous URL contract.** A single `?statut=...` parameter maps to two different behaviors depending on which section's filter was clicked. The same value can mean "filter active loans" or "filter history" depending on the click origin, which makes the URL hard to read and to bookmark.
- **Inconsistent counts.** The header counter shows the filtered count, so it changes every time the user clicks a filter. This hides the section's real total and creates a moving number that suggests state changes when only the view has changed.
- **Partial reset.** The "Réinitialiser" button only clears the search fields, leaving the active section's filter untouched. There is no single action that brings the user back to a clean "see everything" state.

The goal is to make the Loans page read the way a librarian thinks: filters live next to the data they filter, search is a global lookup tool, and the URL is explicit and bookmarkable.

## Solution

Restructure the Loans page around three principles:

1. **Filters are colocated with their data.** The "Emprunts en cours" section owns its own filter group (Tous / Actifs / En retard) and the "Historique" section owns its own (Tous / Terminés / Rendus en retard). A filter only affects the section it lives in.
2. **Search becomes a global tool.** The two search inputs (user + book) move above the "Nouvel emprunt" form. They filter both sections simultaneously, as one would expect from a "look up a user's or book's loans" action.
3. **URL contract becomes explicit.** Two independent parameters (`statutActif` and `statutHistorique`) replace the single `statut`. Each is honored only by the section it describes. A single "Réinitialiser" button clears everything in one click.

The change is purely in the presentation layer and the routing contract. The persistence layer is untouched: no schema change, no new query, no new index. A deep module (`LoanFilter`) encapsulates the "what does this filter mean" knowledge, making the filter logic testable without Spring or the database.

## User Stories

1. As a librarian, I want the Loans page to load the same way every time, so that I know where to find each piece of information.
2. As a librarian, I want a global search bar at the top of the page, so that I can look up a user or a book without scrolling.
3. As a librarian, I want the search to filter both active loans and history at the same time, so that one query gives me the full picture for a user or a book.
4. As a librarian, I want to search by user name AND book title together, so that I can answer precise questions like "did David borrow 1984".
5. As a librarian, I want the "Emprunts en cours" section to have only filters that make sense for active loans (Tous / Actifs / En retard), so that I never click a filter that empties the section by definition.
6. As a librarian, I want the "Historique" section to have only filters that make sense for completed loans (Tous / Terminés / Rendus en retard), so that I can find past loans by their return status.
7. As a librarian, I want a filter click to affect only the section where I clicked it, so that filtering history does not hide active loans and vice versa.
8. As a librarian, I want the section header counter to show the section's total, independent of the filter, so that I always know how many loans are in that section overall.
9. As a librarian, I want a distinct message when a section is genuinely empty versus when the filter has no matches, so that I do not waste time looking for data that does not exist.
10. As a librarian, I want a single "Réinitialiser" button that clears the search and resets both filters, so that I can return to the canonical "see everything" state in one click.
11. As a librarian, I want the URL of the Loans page to be explicit and bookmarkable, so that I can save an "always show me overdue loans" view in my browser.
12. As a librarian, I want the new-loan form to stay at the top of the page, so that creating a loan remains the primary action of the screen.
13. As a librarian, I want the search to ignore blank inputs, so that leaving one field empty searches only the other.
14. As a librarian, I want a search with no results to be distinguishable from "no data exists", so that I know whether to refine the search or accept the empty result.
15. As a librarian, I want pagination to persist across filter changes within the history section, so that I do not lose my scroll position when switching history filters.
16. As a librarian, I want the Loans page to be responsive on mobile, so that the new layout still works on a phone or tablet.
17. As a maintainer, I want the filter logic to be encapsulated in a deep module, so that adding a new filter is a one-place change and does not require touching the service, the controller, or the template switch statements.
18. As a maintainer, I want the new module to be testable without Spring or the database, so that its behavior is fast to verify and not coupled to infrastructure.
19. As a maintainer, I want the URL contract to be self-documenting, so that the controller signature matches the names in the URL.
20. As a maintainer, I want the existing behavior of the active section ("Tous" shows all active loans, "Actifs" shows only non-overdue, "En retard" shows only overdue) to be preserved, so that the refactor does not change operational meaning.
21. As a maintainer, I want the existing behavior of the history section (paginated, filterable, with the existing "Terminé" / "Rendu en retard" badges) to be preserved, so that the refactor does not change historical reporting.
22. As a maintainer, I want the controller to remain free of business logic, so that it only translates URL parameters into service calls and service results into model attributes.
23. As a maintainer, I want the test patterns established by the current `LoanActivityServiceTest` and `EmpruntControllerTest` to be reused, so that the refactor does not introduce a new testing paradigm.
24. As a future agent, I want the Loans page modules to expose narrow interfaces, so that AI tools and new contributors can navigate them without learning the full implementation.

## Implementation Decisions

### Module: `LoanFilter` (deep module, sealed type)

Extract a sealed type hierarchy that encapsulates the "what does this filter mean" knowledge. Two families, one per section:

- `ActiveLoanFilter` with cases `All`, `Active`, `Overdue`
- `HistoryFilter` with cases `All`, `Completed`, `LateReturn`

Each case exposes:
- a stable query-string value (for URL serialization)
- a `matches(Emprunt)` predicate (for in-memory filtering when needed)

A static factory method on each sealed type (`fromQueryParam(String)`) parses the URL value, defaulting to `All` on missing or unknown values. The two families are independent: an `ActiveLoanFilter` does not know about history values, and vice versa.

This is the deep module of the refactor. It encapsulates the mapping between URL value, UI label, and loan-matching rule, and it can be unit-tested in milliseconds without Spring, JPA, or the database.

### Module: `LoanActivityService` (refactored)

The service signature changes from `getLoanActivity(searchUser, searchBook, page, statut)` to `getLoanActivity(searchUser, searchBook, page, statutActif, statutHistorique)`.

Internally, the service:
1. Resolves each query parameter to its corresponding `LoanFilter` value via the new module.
2. Calls the existing repository methods (`findActiveLoans` or `findActiveLoansFiltered` for active, `findHistoryPaged` and `countHistoryFiltered` for history) with the search criteria and pagination.
3. Maps active loans to `ActiveLoan` DTOs as today.
4. Maps history to `LoanHistory` DTOs as today.
5. Returns the `LoanActivity` aggregate (unchanged shape).

The string-based `switch` statements on `statut` disappear. The `toHistoryStatut` and `mapActiveLoans` switches become thin orchestrators that delegate the "is this loan in this filter" question to the `LoanFilter` module.

### Module: `EmpruntController` (routing contract)

The `GET /emprunts` handler takes two `@RequestParam` values instead of one:

- `statutActif` (default `tous`)
- `statutHistorique` (default `tous`)

These are passed straight to the service. No business logic, no transformation, no defensive coding — the controller is a pure translation layer between HTTP and the service.

Pagination, search (`searchUser`, `searchBook`), and the page number remain as they are. The pagination links in the template preserve all three status parameters (the one for the current section's filter plus the other section's filter as a passthrough).

### Module: Templates (single file `liste.html`)

The template layout is reorganized into a fixed order:

1. Page title and subtitle
2. New loan form (existing, unchanged)
3. Search bar (moved up from the history section): two inputs (user, book), "Rechercher" and "Réinitialiser" buttons
4. "Emprunts en cours" section: title, total counter, filter group (Tous / Actifs / En retard), table or empty state
5. "Historique" section: title, total counter, filter group (Tous / Terminés / Rendus en retard), table or empty state, pagination

The filter group CSS class (`status-filters`) and its active state are reused. Each filter button becomes an anchor that sets its section's parameter and preserves the other section's parameter and the search fields.

The two distinct empty states:
- "Genuinely empty": when the section's data is empty regardless of filter ("Tous les livres ont été retournés" / "Aucun emprunt n'a encore été enregistré").
- "Filter has no matches": when the data exists in other states but the current filter excludes them. The message names the active filter so the user understands why.

### Architectural Decisions

- **No schema change.** The persistence layer is untouched. The refactor is a presentation-layer and routing refactor.
- **No new query, no new index.** The repository methods that already exist (`findActiveLoans`, `findActiveLoansFiltered`, `findHistoryPaged`, `countHistoryFiltered`) are reused as-is.
- **No live refresh.** A loan that changes state while the user is on the page is reflected on the next full page load. No client-side polling or magic updates.
- **No "X masqué par le filtre" note.** The filter is a view, not a data scope. Hiding rows that do not match is expected behavior.
- **Counter is the section's total.** The header counter shows the total number of loans in the section, unchanged by the active filter. This gives the user a stable reference point.
- **Single "Réinitialiser" button.** It clears the search and resets both filters to "Tous" by linking to the canonical URL `/emprunts` with no parameters.
- **URL parameters are camelCase.** `statutActif`, `statutHistorique`, `searchUser`, `searchBook`, `page`. The names match the query parameter names in the controller signature for self-documentation.
- **Filters are URL-stable.** Each filter value is a stable enum-like string (`tous`, `actifs`, `en_retard`, `termines`, `rendus_en_retard`) used both in the URL and in the `LoanFilter` sealed type's serialization.

### API Contract

The `GET /emprunts` endpoint accepts the following query parameters, all optional:

| Parameter | Default | Allowed values | Effect |
|---|---|---|---|
| `statutActif` | `tous` | `tous`, `actifs`, `en_retard` | Filters the "Emprunts en cours" section |
| `statutHistorique` | `tous` | `tous`, `termines`, `rendus_en_retard` | Filters the "Historique" section |
| `searchUser` | (none) | free text, case-insensitive | AND-filter on user name in both sections |
| `searchBook` | (none) | free text, case-insensitive | AND-filter on book title in both sections |
| `page` | `0` | non-negative integer | History pagination index |

The response is the same rendered Thymeleaf view as today. No JSON API is introduced.

## Testing Decisions

### What Makes a Good Test

A test verifies behavior at the module's public seam, not its internal assembly. Tests should not pin implementation details: when the `LoanFilter` module is reorganized internally, the tests should not need to change. When the `LoanActivityService` swaps the order of its two repository calls, the tests should still pass.

### Module: `LoanFilter` (new tests, no Spring, no DB)

Unit tests, plain JUnit, no mocking:

1. `ActiveLoanFilter.fromQueryParam("tous")` returns `All`
2. `ActiveLoanFilter.fromQueryParam("actifs")` returns `Active`
3. `ActiveLoanFilter.fromQueryParam("en_retard")` returns `Overdue`
4. `ActiveLoanFilter.fromQueryParam(null)` and `fromQueryParam("")` and `fromQueryParam("garbage")` all return `All`
5. `ActiveLoanFilter.Active.matches(emprunt)` returns `true` only when the loan has no actual return date and the expected return date is today or later
6. `ActiveLoanFilter.Overdue.matches(emprunt)` returns `true` only when the loan has no actual return date and the expected return date is before today
7. `ActiveLoanFilter.All.matches(emprunt)` returns `true` for any active loan
8. Equivalent test set for `HistoryFilter`: `All`, `Completed`, `LateReturn`, default fallback, and `matches` behavior

These tests are pure logic, milliseconds-fast, and cover the most important knowledge in the refactor.

### Module: `LoanActivityService` (updated tests)

Reuse the existing `LoanActivityServiceTest` pattern (Mockito on the repository). The signature change forces an update, but the spirit of the tests stays the same:

1. The service calls the repository with the search parameters for both queries
2. The service calls the history repository with the page number
3. The service forwards the resolved `ActiveLoanFilter` to the active-loan mapping logic
4. The service forwards the resolved `HistoryFilter` to the history repository call
5. The service returns the existing `LoanActivity` aggregate with the right counts
6. Blank search inputs are ignored (existing test, preserved)
7. The total active count comes from the unfiltered active query, regardless of the active filter
8. The total history count comes from `countHistoryFiltered` with the history filter applied

### Module: `EmpruntController` (updated tests)

Reuse the existing `EmpruntControllerTest` pattern (MockMvc with standalone setup and a mocked service):

1. `GET /emprunts` with no parameters calls the service with `statutActif=tous` and `statutHistorique=tous`
2. `GET /emprunts?statutActif=en_retard` calls the service with `statutActif=en_retard` and the default history status
3. `GET /emprunts?statutHistorique=termines` calls the service with `statutHistorique=termines` and the default active status
4. `GET /emprunts?searchUser=David&searchBook=1984` forwards both search terms
5. `GET /emprunts?page=2` forwards the page number
6. The rendered view name is unchanged (`emprunts/liste`)
7. The model contains the same keys as today

### Module: Template (visual regression not in scope)

The template change is exercised by the existing controller tests' response body checks. No new template-engine unit tests are introduced in this PRD. Visual verification is done manually in the browser.

### Prior Art

- `LoanActivityServiceTest` — Mockito-based behavior tests, will be the model for the updated service tests.
- `EmpruntControllerTest` — MockMvc standalone setup, will be the model for the updated controller tests.
- `EmpruntRepositoryPostgresTest` — Testcontainers integration tests, unchanged, because the repository is unchanged.
- `PostgresIntegrationTestBase` — shared base for Testcontainers tests, unchanged.

## Out of Scope

- Persisting the last-used filter per agent (no per-user filter memory).
- Live search (the search submits on button click, not on every keystroke).
- Date-range filters in the history section.
- Exporting the loans list (CSV, PDF).
- Splitting the Loans page into separate URLs for active and history.
- Renaming the existing controller, service, or repository to use the "Loan / Book / User" vocabulary from `CONTEXT.md` (already a project-wide concern, not specific to this refactor).
- Paginating the active loans section (decided against previously and out of scope for this refactor).
- Changes to the new-loan form's behavior, fields, or validation.
- Changes to the actual return flow (`POST /emprunts/{id}/retour`).
- Changes to the audit trail display on the loan detail page.
- Adding a "saved views" or "favorites" feature for filter combinations.
- Mobile-specific redesign (the existing responsive CSS is reused; no new breakpoints).

## Further Notes

- The single most important benefit of this refactor is **eliminating the "click a filter in section A and see the effect in section B" trap**. The new design makes the cause and effect local to the same section, which is how a librarian already thinks.
- The `LoanFilter` deep module is the leverage point of this refactor. Once it exists, adding a new status filter is a one-line addition in the sealed type, one test case, and one button in the template. The service, the controller, and the repository are not touched.
- The refactor is intentionally small in code volume but large in user-facing impact. The bulk of the change is in the template (move search bar, split filter groups, update pagination links) and in the test signatures.
- The recommended implementation order is:
  1. Introduce the `LoanFilter` sealed type and its unit tests.
  2. Update the `LoanActivityService` signature and internals, adapting the existing tests.
  3. Update the `EmpruntController` signature, adapting the existing tests.
  4. Update the `liste.html` template: move the search bar, split the filter group, update the pagination links, add the second empty state.
  5. Run the full test suite to verify no regression on the existing 260 tests.
- The "Actifs" filter in the active section means "active and not overdue". The "En retard" filter means "active and overdue". The "Tous" filter in the active section means "all active loans, overdue or not". This is preserved from the current behavior and is the operational meaning a librarian expects.
- The "Terminés" filter in the history section means "completed and returned on or before the expected return date". The "Rendus en retard" filter means "completed and returned after the expected return date". The "Tous" filter in the history section means "all completed loans, returned on time or late". This is preserved from the current behavior and matches the existing badges.
- The page order (form, search, active, history) reflects the librarian's mental priority: create a loan first, then look up information. This order is confirmed during the design phase and should not be reversed without further discussion.
