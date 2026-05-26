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
Un **Emprunt** actif dont la durée dépasse la règle métier de retard. La règle actuelle est basée sur 30 jours.
_Avoid_: Retard calculé dans l'écran

**Activité des emprunts**:
La vue opérationnelle des emprunts utilisée par les écrans pour suivre les emprunts actifs, les retards, l'historique et les indicateurs d'accueil. Elle décrit l'activité observable, pas la commande qui crée ou retourne un emprunt.
_Avoid_: Page Emprunts, dashboard service, reporting générique

**Date de retour prévue**:
La date attendue de retour d'un **Emprunt**, connue au moment de la création ou ajoutée ensuite. Elle est distincte de la date de retour effective qui clôt l'emprunt.
_Avoid_: Date retour quand l'ambiguïté avec la date effective est possible

**Date de retour effective**:
La date à laquelle le **Livre** est réellement retourné et l'**Emprunt** devient terminé.
_Avoid_: Date de retour seule quand une date prévue existe aussi

## Flagged ambiguities

**Date de retour**:
Le terme est ambigu dès que le système accepte une date prévue. Utiliser **Date de retour prévue** pour l'engagement attendu et **Date de retour effective** pour la clôture réelle.

## Example dialogue

Dev: Pour l'Accueil, tu veux afficher quoi autour des emprunts ?
Expert: L'Activité des emprunts : les emprunts actifs, les emprunts en retard et quelques indicateurs.
Dev: Et pour créer un emprunt, on utilise aussi cette activité ?
Expert: Non, la création vérifie la Disponibilité du Livre et peut saisir une Date de retour prévue. L'Activité des emprunts sert à consulter et filtrer ce qui existe.
Dev: Quand le livre revient, quelle date renseigne-t-on ?
Expert: La Date de retour effective. Elle termine l'Emprunt et rend le Livre disponible.
