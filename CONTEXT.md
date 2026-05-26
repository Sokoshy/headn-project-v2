# Gestionnaire de Bibliothèque

Contexte métier du système de gestion de bibliothèque. Ce vocabulaire sert à nommer les modules, les écrans et les règles autour du catalogue, des utilisateurs et des emprunts.

## Language

**Livre**:
Un ouvrage du catalogue qui peut être disponible ou engagé dans un emprunt actif. Un **Livre** peut avoir zéro ou plusieurs **Emprunts** dans son historique.
_Avoid_: Book dans l'interface utilisateur, ouvrage quand le code parle du modèle principal

**Utilisateur**:
Une personne inscrite qui peut emprunter des livres. Un **Utilisateur** peut avoir zéro ou plusieurs **Emprunts**.
_Avoid_: Membre, lecteur, user dans l'interface utilisateur

**Emprunt**:
L'enregistrement métier qui relie un **Utilisateur** à un **Livre** depuis une date d'emprunt jusqu'à une date de retour effective. Un **Emprunt** est actif tant qu'il n'a pas de date de retour effective.
_Avoid_: Prêt, loan dans l'interface utilisateur

**Disponibilité**:
L'état métier dérivé indiquant qu'un **Livre** n'a aucun **Emprunt** actif et peut être emprunté. La disponibilité ne doit pas être traitée comme une saisie utilisateur.
_Avoid_: Stock, flag disponible

**Historique des emprunts**:
L'ensemble des **Emprunts** terminés, conservés pour comprendre l'activité passée d'un livre ou d'un utilisateur.
_Avoid_: Archives, logs

**Emprunt en retard**:
An active **Emprunt** whose **Date de retour prévue** is earlier than today. It represents an operational situation where the book is still expected back.
_Avoid_: Retard calculé depuis la date d'emprunt, Emprunt rendu en retard

**Activité des emprunts**:
La vue opérationnelle des emprunts utilisée par les écrans pour suivre les emprunts actifs, les retards, l'historique et les indicateurs d'accueil. Elle décrit l'activité observable, pas la commande qui crée ou retourne un emprunt.
_Avoid_: Page Emprunts, dashboard service, reporting générique

**Date de retour prévue**:
The expected return date of an **Emprunt**, required when the loan is created. It is distinct from the **Date de retour effective** that completes the loan and may be corrected while the loan is active.
_Avoid_: Date retour when ambiguity with the actual return date is possible

**Correction de Date de retour prévue**:
A correction applied to an active **Emprunt** when its **Date de retour prévue** was entered incorrectly. It can move the expected return date earlier or later, but it is not a business extension or shortening of the loan.
_Avoid_: Prolongation, raccourcissement, silent edit

**Date de retour effective**:
The date on which the **Livre** is actually returned and the **Emprunt** becomes completed.
_Avoid_: Date de retour seule quand une date prévue existe aussi

**Emprunt rendu en retard**:
A completed **Emprunt** whose **Date de retour effective** is later than its **Date de retour prévue**. It represents historical lateness, not an active operational situation.
_Avoid_: Emprunt en retard when the loan is already completed

## Flagged ambiguities

**Date de retour**:
This term is ambiguous once the system accepts an expected return date. Use **Date de retour prévue** for the expected commitment and **Date de retour effective** for the actual completion date.

**Retard**:
This term is ambiguous between an active overdue loan and historical lateness. Use **Emprunt en retard** for active overdue loans and **Emprunt rendu en retard** for completed loans returned after the expected date.

## Example dialogue

Dev: Pour l'Accueil, tu veux afficher quoi autour des emprunts ?
Expert: L'Activité des emprunts : les emprunts actifs, les emprunts en retard et quelques indicateurs.
Dev: Et pour créer un emprunt, on utilise aussi cette activité ?
Expert: Non, la création vérifie la Disponibilité du Livre et peut saisir une Date de retour prévue. L'Activité des emprunts sert à consulter et filtrer ce qui existe.
Dev: Quand le livre revient, quelle date renseigne-t-on ?
Expert: La Date de retour effective. Elle termine l'Emprunt et rend le Livre disponible.
Dev: Si le livre revient après la Date de retour prévue, comment le nomme-t-on ?
Expert: C'est un Emprunt rendu en retard. Un Emprunt en retard, lui, est encore actif et attend toujours son retour.
