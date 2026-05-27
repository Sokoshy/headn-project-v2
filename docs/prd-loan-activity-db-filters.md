# PRD — LoanActivityService : Filtres et pagination côté DB

## Problem Statement

Le `LoanActivityService` charge aujourd'hui **tous les emprunts** depuis la base de données (`findAllWithDetails`), puis effectue le filtrage par statut, la recherche utilisateur/livre et la pagination **en mémoire**. Ce pattern :
- Consomme de la mémoire proportionnellement au nombre total d'emprunts
- Ne scale pas : la performance dégrade linéairement avec le volume
- Mélange les responsabilités : le service fait le travail de la couche persistence
- Complique les tests : les mocks doivent simuler des listes volumineuses

## Solution

Déplacer la logique de filtrage et de pagination dans le repository via des requêtes JPQL paramétrées. Le service devient un orchestrateur simple qui compose des appels au repository et fait le mapping Entité→DTO.

Décisions prises lors de la revue d'architecture :
- **Emprunts en cours** : pas de pagination, affichage complet (vue opérationnelle)
- **Filtres de statut** : un filtre = une seule liste (pas de section masquée)
- **Mapping** : reste dans le service (toActiveLoan / toLoanHistory)

## User Stories

1. As a **bibliothécaire**, I want to see active loans displayed instantly without loading all historical data, so that I can process returns quickly
2. As a **bibliothécaire**, I want to filter loans by status (actifs, en_retard, termines, rendus_en_retard), so that I can focus on what matters
3. As a **bibliothécaire**, I want to search the loan history by user name or book title, so that I can find specific past loans
4. As a **bibliothécaire**, I want to paginate through loan history, so that pages load quickly even with thousands of records
5. As a **bibliothécaire**, I want the "Tous" filter to show both active loans and history together, so that I get a complete overview
6. As a **developer**, I want the repository to handle filtering and pagination, so that the service layer stays focused on business logic
7. As a **developer**, I want the loan activity tests to remain behavior-focused, so that internal refactors don't break tests
8. As a **developer**, I want the mapping from Emprunt to ActiveLoan/LoanHistory to stay in the service, so that the repository stays focused on data access

## Implementation Decisions

### Architecture Changes

**EmpruntRepository** — new methods:
- `findActiveLoans()` : returns all active loans (no pagination), ordered by dateEmprunt ASC
- `findActiveLoans(searchUser, searchBook)` : active loans filtered by user/book (no pagination)
- `findHistoryPaged(searchUser, searchBook, statut, pageable)` : history with filters + pagination
- `countHistoryFiltered(searchUser, searchBook, statut)` : count for pagination metadata

**LoanActivityService** — simplified `getLoanActivity`:
- Calls `findActiveLoans()` (with optional search params)
- Calls `findHistoryPaged(...)` with Pageable
- Calls `countHistoryFiltered(...)` for total elements
- Maps results via existing `toActiveLoan` / `toLoanHistory` methods
- Returns `LoanActivity` record (unchanged)

### Status Filter Mapping

Each status filter maps to a specific query condition:
- `"tous"` or null → no filter (active + all history)
- `"actifs"` → `dateRetour IS NULL AND dateRetourPrevue >= TODAY`
- `"en_retard"` → `dateRetour IS NULL AND dateRetourPrevue < TODAY`
- `"termines"` → `dateRetour IS NOT NULL AND dateRetour <= dateRetourPrevue`
- `"rendus_en_retard"` → `dateRetour IS NOT NULL AND dateRetour > dateRetourPrevue`

### Search Behavior

- Search applies **only to history** (existing behavior preserved)
- Active loans are not filtered by search (operational view)
- Search is case-insensitive, partial match (LIKE %term%)

### Pagination

- Page size: 10 (existing DEFAULT_PAGE_SIZE)
- Only history is paginated
- Active loans always shown in full
- Page clamping: requested page > totalPages → page 0

### Template Changes

The status filter behavior changes: each filter shows only ONE section. The template needs minor updates to handle the new single-list-per-filter behavior.

## Testing Decisions

### Test Strategy (TDD)

Tests verify behavior through public interfaces only. One test = one behavior. Vertical slices.

### Unit Tests (Mockito — LoanActivityServiceTest)

1. **getLoanActivity_callsRepositoryForActiveLoans** — verifies the service calls `findActiveLoans()` and maps results
2. **getLoanActivity_callsRepositoryForHistoryWithPageable** — verifies pagination params forwarded to repository
3. **getLoanActivity_passesSearchFiltersToRepository** — searchUser/searchBook forwarded to both active and history queries
4. **getLoanActivity_passesStatutToRepository** — statut forwarded to history query
5. **getLoanActivity_mapsActiveLoansAndHistory** — DTO mapping verified (existing tests adapted)
6. **getLoanActivity_clampsPageToValidRange** — page out of bounds → page 0
7. **getLoanActivity_ignoresBlankSearchTerms** — blank strings treated as null

### Integration Tests (Testcontainers — EmpruntRepositoryPostgresTest)

8. **findActiveLoans_returnsOnlyUnreturnedLoans** — real DB query verification
9. **findActiveLoans_filtersByUserAndBook** — search filter with real data
10. **findHistoryPaged_appliesStatutFilter** — each statut value returns correct subset
11. **findHistoryPaged_appliesPagination** — correct page content and total count
12. **countHistoryFiltered_returnsCorrectCount** — matches findHistoryPaged results

### Prior Art

- Existing tests in `LoanActivityServiceTest` (Mockito-based, behavior-focused)
- Existing integration tests in `EmpruntRepositoryPostgresTest` (Testcontainers)
- Pattern: `PostgresIntegrationTestBase` for shared DB setup

## Out of Scope

- Paginating active loans (decided against for current scale)
- Filtering active loans by search (operational view should stay complete)
- Spring Data Projections (mapping stays in service)
- Changing the `LoanActivity` record structure
- Changing the template layout beyond filter behavior

## Further Notes

- The `findAllWithDetails()` method in the repository should be kept for backward compatibility but will no longer be called by `LoanActivityService`
- The `findEmpruntsActifs()` and `findHistorique()` methods can remain as they are used elsewhere
- Performance improvement is most significant when total emprunts > 100
