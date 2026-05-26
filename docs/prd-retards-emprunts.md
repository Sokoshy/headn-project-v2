# PRD — Expected Return Dates and Loan Lateness

## Problem Statement

Library staff need the system to distinguish loans that require operational action now from loans that were returned late in the past. The current lateness rule is based on a fixed 30-day duration from the borrow date, while the desired business rule is based on the required **Date de retour prévue** for each **Emprunt**.

Because **Date de retour prévue** currently exists but is optional, the system can create loans without a clear return commitment. This makes **Emprunt en retard** ambiguous and prevents reliable historical reporting for **Emprunt rendu en retard**.

## Solution

Make **Date de retour prévue** a required commitment for every **Emprunt**, defaulted in the UI to today plus 30 days but validated explicitly by the server. Lateness will be calculated from dates rather than persisted as a status.

The system will support two separate business concepts:

- **Emprunt en retard**: an active **Emprunt** whose **Date de retour prévue** is earlier than today.
- **Emprunt rendu en retard**: a completed **Emprunt** whose **Date de retour effective** is later than its **Date de retour prévue**.

Existing loans without an expected return date will be migrated by setting **Date de retour prévue** to borrow date plus 30 days, then the database column will become required. Library staff will also be able to correct an expected return date for an active loan when it was entered incorrectly.

## User Stories

1. As a library staff member, I want every new **Emprunt** to have a **Date de retour prévue**, so that each loan has a clear return commitment.
2. As a library staff member, I want the new loan form to suggest a **Date de retour prévue** of today plus 30 days, so that normal loans are quick to create.
3. As a library staff member, I want to modify the suggested **Date de retour prévue** before creating the loan, so that exceptional loan durations can be entered.
4. As a library staff member, I want the system to reject a missing **Date de retour prévue**, so that loans are never created without a return commitment.
5. As a library staff member, I want the system to reject a **Date de retour prévue** in the past, so that a new loan is not created already overdue by mistake.
6. As a library staff member, I want a **Date de retour prévue** equal to today to be accepted, so that same-day loans are possible.
7. As a library staff member, I want an active loan whose **Date de retour prévue** is before today to be marked **En retard**, so that I know it requires action.
8. As a library staff member, I want an active loan whose **Date de retour prévue** is today to remain **Actif**, so that due-today loans are not prematurely marked late.
9. As a library staff member, I want an active loan whose **Date de retour prévue** is in the future to be marked **Actif**, so that normal ongoing loans are clear.
10. As a library staff member, I want a returned loan whose **Date de retour effective** is after its **Date de retour prévue** to be marked **Rendu en retard**, so that late returns remain visible historically.
11. As a library staff member, I want a returned loan whose **Date de retour effective** is equal to its **Date de retour prévue** to be treated as on time, so that due-date returns are not incorrectly penalized.
12. As a library staff member, I want a returned loan whose **Date de retour effective** is before its **Date de retour prévue** to be treated as on time, so that early returns are clear.
13. As a library staff member, I want the normal return action to set **Date de retour effective** to today, so that returning a book remains a simple operational action.
14. As a library staff member, I want the dashboard count for overdue loans to include only active **Emprunts en retard**, so that the dashboard highlights work needing action now.
15. As a library staff member, I do not want historical late returns counted in the dashboard overdue count, so that operational and historical lateness are not mixed.
16. As a library staff member, I want the loan list to show **Retour prévu**, so that the displayed status has visible context.
17. As a library staff member, I want overdue active loans to be visually highlighted in the active loans list, so that I can identify them quickly.
18. As a library staff member, I want completed late loans to be visually highlighted in the history list, so that historical lateness is visible.
19. As a library staff member, I want to filter loans by **Tous**, so that I can see all loan activity.
20. As a library staff member, I want to filter loans by **Actifs**, so that I can focus on ongoing loans.
21. As a library staff member, I want to filter loans by **En retard**, so that I can focus on overdue active loans.
22. As a library staff member, I want to filter loans by **Terminés**, so that I can focus on completed loans.
23. As a library staff member, I want to filter loans by **Rendus en retard**, so that I can analyze historical late returns.
24. As a library staff member, I want to correct the **Date de retour prévue** of an active loan from its detail page, so that data-entry mistakes can be fixed in context.
25. As a library staff member, I want correction of **Date de retour prévue** to allow moving the date earlier or later, so that both overly long and overly short accidental entries can be corrected.
26. As a library staff member, I want correction of **Date de retour prévue** to be available only while the **Emprunt** is active, so that completed history is not silently rewritten.
27. As a library staff member, I want correction of **Date de retour prévue** to reject missing dates, so that the required commitment remains intact.
28. As a library staff member, I want correction of **Date de retour prévue** to reject dates in the past, so that corrections do not create artificial overdue history.
29. As a library staff member, I want correction of **Date de retour prévue** to accept today or a future date, so that valid operational corrections are allowed.
30. As a library staff member, I want the system to show “La date de retour prévue est obligatoire.” when the expected return date is missing, so that I understand how to fix the error.
31. As a library staff member, I want the system to show “La date de retour prévue doit être aujourd’hui ou une date future.” when the expected return date is in the past, so that I understand the allowed range.
32. As a library staff member, I want existing loans to receive a migrated **Date de retour prévue**, so that old data remains usable under the new rules.
33. As a library staff member, I want migrated historical loans to be classified consistently as **Rendu en retard** when applicable, so that old and new history follow the same business rule.
34. As a library staff member, I want no special deletion rule for loans returned late, so that late history does not block deletions differently from normal loan history.
35. As a developer, I want lateness to be calculated from loan dates, so that statuses cannot become inconsistent with their source data.
36. As a developer, I want the domain vocabulary in tests and UI to distinguish **Emprunt en retard** from **Emprunt rendu en retard**, so that future changes preserve the business distinction.

## Implementation Decisions

- **Domain model**: update the **Emprunt** behavior so active lateness is based on **Date de retour prévue** being strictly before today, not on borrow date plus 30 days.
- **Historical lateness**: add behavior for determining whether an **Emprunt** is an **Emprunt rendu en retard** when completed after its expected return date.
- **No persisted status**: do not store overdue or late-return status in the database. Calculate these statuses from **Date de retour prévue**, **Date de retour effective**, and today.
- **Required expected return date**: make **Date de retour prévue** required for new loans at the service/controller boundary and in the database schema.
- **Migration strategy**: backfill existing loans with **Date de retour prévue = date d'emprunt + 30 days**, then make the column non-null.
- **Creation validation**: new loans must provide **Date de retour prévue**. It must be today or a future date. Empty values and past dates return user-facing validation errors.
- **Creation default**: the loan creation UI should prefill **Date de retour prévue** with today plus 30 days. This is a UI default only, not a silent server fallback.
- **Return action**: the normal return flow continues to set **Date de retour effective** to today and does not ask for manual date entry.
- **Correction action**: add a targeted correction operation for **Date de retour prévue** on active loans. It is a correction of data-entry error, not a business extension or shortening of the loan.
- **Correction route contract**: use a POST action for `/emprunts/{id}/date-retour-prevue` with a `dateRetourPrevue` parameter. On success, redirect to the loan detail. On error, return to the detail with a flash error.
- **Correction validation**: correction is allowed only for active loans, requires a value, and requires today or a future date.
- **No correction history**: do not track historical corrections in this PRD. If true business extensions or shortenings are introduced later, they should be modeled explicitly and may need history.
- **Loan activity module**: extend the loan activity view model to represent both active overdue status and completed late-return status using the validated domain terms.
- **Filtering**: add loan activity filtering for **Tous**, **Actifs**, **En retard**, **Terminés**, and **Rendus en retard**.
- **Dashboard**: the dashboard overdue count remains operational and counts only active **Emprunts en retard**.
- **UI labels**: use French labels in the interface: **Actif**, **En retard**, **Terminé**, **Rendu en retard**, and **Retour prévu**.
- **Documentation**: `CONTEXT.md` has been updated with English definitions for **Emprunt en retard**, **Emprunt rendu en retard**, **Date de retour prévue**, and **Correction de Date de retour prévue**.
- **ADR**: no ADR is required. This is a domain rule clarification rather than a hard-to-reverse architectural decision with surprising trade-offs.
- **Potential deep module**: extract or centralize loan status classification behind a small public interface that accepts an **Emprunt** and returns the operational/historical status. This would encapsulate the date comparison rules and make tests focused and stable.

## Testing Decisions

- Use normal TDD with vertical red-green-refactor cycles: write one behavior test, make it fail, implement the minimum code to pass, refactor only when green, then continue.
- Tests should verify external behavior and domain outcomes, not implementation details. They should not assert private methods or internal query shapes unless the observable repository contract requires it.
- Prioritize integration-style or public-interface tests that survive refactoring.
- Model tests should cover:
  - active **Emprunt en retard** when **Date de retour prévue** is before today;
  - not overdue when expected return is today;
  - not overdue when expected return is in the future;
  - not active-overdue once returned;
  - **Emprunt rendu en retard** when effective return is after expected return;
  - on-time historical return when effective return is equal to or before expected return.
- Service tests should cover:
  - creation rejects missing **Date de retour prévue**;
  - creation rejects past **Date de retour prévue**;
  - creation accepts today and future dates;
  - correction succeeds for an active loan with today/future expected date;
  - correction rejects completed loans;
  - correction rejects missing or past dates;
  - dashboard overdue count uses the new expected-return-date rule.
- Repository/integration tests should cover:
  - Flyway migration backfills existing null expected return dates;
  - the expected return date column becomes non-null;
  - overdue active loan queries count/filter by **Date de retour prévue** rather than borrow date.
- Controller tests should cover:
  - creating a loan with a valid expected return date;
  - missing expected return date redirects with “La date de retour prévue est obligatoire.”;
  - past expected return date redirects with “La date de retour prévue doit être aujourd’hui ou une date future.”;
  - correction POST success redirects to detail;
  - correction POST failure redirects/returns with flash error;
  - loan list passes the selected status filter to the activity service.
- View/MockMvc tests should cover the visible labels and filters where existing test patterns support it:
  - creation form prefilled with default expected return date;
  - active badge **En retard**;
  - active badge **Actif**;
  - history badge **Rendu en retard**;
  - history badge **Terminé**;
  - filter options **Tous**, **Actifs**, **En retard**, **Terminés**, **Rendus en retard**.
- Existing prior art in the codebase includes model tests for `Emprunt`, service tests for `EmpruntService` and `LoanActivityService`, controller tests for `EmpruntController` and `HomeController`, and PostgreSQL/Flyway integration tests for repository and schema behavior.

## Out of Scope

- Authentication or role-based access control for correction actions.
- Manual entry of **Date de retour effective** during the normal return flow.
- Business **Prolongation** or business **Raccourcissement** of a loan as first-class concepts.
- Audit history for corrections of **Date de retour prévue**.
- Dashboard counters for **Emprunts rendus en retard**.
- Special deletion rules based on historical lateness.
- Persisting overdue/late-return status in the database.
- Editing completed loan history through the normal UI.
- Changing the existing French route naming conventions outside the new correction route.

## Further Notes

- Documentation output should be in English; chat with the user remains in French.
- UI labels remain in French.
- The implementation should respect the layered architecture: controllers delegate business rules to services, and repositories remain persistence-focused.
- All new POST forms must include CSRF tokens.
- User-facing messages should use flash attributes on redirects.
- The likely TDD order is:
  1. active lateness based on **Date de retour prévue**;
  2. historical lateness based on **Date de retour effective**;
  3. required expected return date on creation;
  4. expected return date cannot be in the past;
  5. migration backfill and non-null constraint;
  6. creation UI default of today plus 30 days;
  7. correction action for active loans;
  8. list badges and status filters;
  9. dashboard active-overdue count only.
