# Library Management System

Business context for the library management system. This vocabulary is used to name modules, screens, and rules around the catalog, users, and loans.

## Language

**Book**:
A catalog item that can be available or engaged in an active loan. A **Book** can have zero or more **Loans** in its history.
_Avoid_: livre in the user interface, ouvrage when the code refers to the main model

**User**:
A registered person who can borrow books. A **User** can have zero or more **Loans**.
_Avoid_: Membre, lecteur, utilisateur in the user interface

**Loan**:
The business record that links a **User** to a **Book** from a loan date until an actual return date. A **Loan** is active as long as it has no actual return date.
_Avoid_: emprunt, prêt in the user interface

**Availability**:
The derived business state indicating that a **Book** has no active **Loan** and can be borrowed. Availability should not be treated as user input.
_Avoid_: Stock, disponible flag

**Loan History**:
The set of completed **Loans**, kept to understand the past activity of a book or a user.
_Avoid_: Archives, logs

**Overdue Loan**:
An active **Loan** whose **Expected Return Date** is earlier than today. It represents an operational situation where the book is still expected back.
_Avoid_: Retard calculé depuis la date d'emprunt, Late Return

**Loan Activity**:
The operational view of loans used by screens to track active loans, overdue loans, history, and home indicators. It describes observable activity, not the command that creates or returns a loan.
_Avoid_: Page Emprunts, dashboard service, generic reporting

**Expected Return Date**:
The expected return date of a **Loan**, required when the loan is created. It is distinct from the **Actual Return Date** that completes the loan and may be corrected while the loan is active.
_Avoid_: Date retour when ambiguity with the actual return date is possible

**Expected Return Date Correction**:
A correction applied to an active **Loan** when its **Expected Return Date** was entered incorrectly. It can move the expected return date earlier or later, but it is not a business extension or shortening of the loan.
_Avoid_: Prolongation, raccourcissement, silent edit

**Actual Return Date**:
The date on which the **Book** is actually returned and the **Loan** becomes completed.
_Avoid_: Date de retour seule quand une date prévue existe aussi

**Late Return**:
A completed **Loan** whose **Actual Return Date** is later than its **Expected Return Date**. It represents historical lateness, not an active operational situation.
_Avoid_: Overdue Loan when the loan is already completed

**Agent**:
A staff member of the library who can perform actions on the system (create loans, return books, manage the catalog). An **Agent** has a **Role** that determines their permissions. An **Agent** is distinct from a **User** (borrower).
_Avoid_: Bibliothécaire, staff, user in the user interface

**Role**:
The classification of an **Agent** that determines what they can do in the system. Roles are **Librarian** (all actions except agent management) and **Admin** (all actions including agent management).
_Avoid_: Permission, access level

**Audit Trail**:
The history of actions performed by **Agents** on **Loans**. Each entry records which **Agent** performed which **Action** (creation, return) and when. The trail can be viewed per loan or globally.
_Avoid_: Logs, action history

**Audit Action**:
A type of event recorded in the **Audit Trail**. Current actions are **Creation** (loan validation) and **Return** (book return validation).
_Avoid_: Event, operation

## Flagged Ambiguities

**Return Date**:
This term is ambiguous once the system accepts an expected return date. Use **Expected Return Date** for the expected commitment and **Actual Return Date** for the actual completion date.

**Overdue**:
This term is ambiguous between an active overdue loan and historical lateness. Use **Overdue Loan** for active overdue loans and **Late Return** for completed loans returned after the expected date.

## Example Dialogue

Dev: For the Home screen, what do you want to show around loans?
Expert: The Loan Activity: active loans, overdue loans, and some indicators.
Dev: And for creating a loan, do we also use this activity?
Expert: No, creation checks the Book's Availability and can capture an Expected Return Date. Loan Activity is for viewing and filtering existing loans.
Dev: When the book comes back, what date do we enter?
Expert: The Actual Return Date. It completes the Loan and makes the Book available.
Dev: If the book comes back after the Expected Return Date, what do we call it?
Expert: That's a Late Return. An Overdue Loan is still active and waiting to be returned.
