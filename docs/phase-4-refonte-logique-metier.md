# Phase 4 — Refonte de la logique metier

## Objectif

Centraliser toutes les regles metier dans des services clairs et testables.

---

## Decisions prises

### Architecture des services

- Trois services crees : `LivreService`, `UtilisateurService`, `EmpruntService`.
- Chaque service est annote `@Service` avec `@Transactional(readOnly = true)` par defaut.
- Les methodes modifiantes utilisent `@Transactional` en ecriture.
- Les services dependants sont injectes via constructeur (injection de dependance).

### Separation des responsabilites

- Les services encapsulent toute la logique metier.
- Les repositories sont exclusivement utilises par les services.
- Les controllers (Phase 5) consommeront uniquement les services.
- Aucun acces direct aux repositories depuis les controllers.

### Gestion des exceptions metier

- Hierarchie d'exceptions creee dans `com.bibliotheque.exception`.
- `BusinessException` : exception de base avec code d'erreur, pour toutes les violations de regles metier.
- `ResourceNotFoundException` : ressource introuvable (avec message adapte par type de recherche : ID ou email).
- Exceptions specifiques : `LivreNotFoundException`, `UtilisateurNotFoundException`, `EmpruntNotFoundException`.
- Exceptions metier : `LivreNonDisponibleException`, `EmailDejaUtiliseException`, `EmpruntDejaRetourneException`, `DuplicateResourceException`.
- Les suppressions impossibles utilisent `BusinessException` avec le code `"SUPPRESSION_IMPOSSIBLE"`.

### Regles metier implementees

#### LivreService

| Regle | Implementation |
|-------|----------------|
| Unicite titre + auteur | Verifie avant creation et modification (match exact apres filtrage LIKE) |
| Disponibilite | Determinee a partir de l'absence d'emprunt actif, avec synchronisation du flag `disponible` |
| Suppression | Impossible si un historique d'emprunts existe (exception `SUPPRESSION_IMPOSSIBLE`) |
| Recherche | Par titre ou auteur (insensible a la casse) |

#### UtilisateurService

| Regle | Implementation |
|-------|----------------|
| Unicite email | Verifie avant creation et modification (normalisation lowercase) |
| Modification email | Verification uniquement si l'email change |
| Normalisation email | Emails stockes en lowercase + trim |
| Suppression | Impossible si un historique d'emprunts existe |
| Recherche | Par nom ou email (insensible a la casse) |

#### EmpruntService

| Regle | Implementation |
|-------|----------------|
| Disponibilite livre | Verifie l'absence d'emprunt actif sous verrou pessimiste |
| Creation | Marque le livre comme indisponible et force une ecriture immediate |
| Retour | Recalcule la disponibilite a partir des emprunts actifs restants |
| Retour double | Impossible via `EmpruntDejaRetourneException` |
| Emprunts en retard | Calcul base sur 30 jours (requete COUNT efficace) |

---

## Changements effectues

### Exceptions metier

| Fichier | Description |
|---------|-------------|
| `src/main/java/com/bibliotheque/exception/BusinessException.java` | Exception de base avec code d'erreur |
| `src/main/java/com/bibliotheque/exception/ResourceNotFoundException.java` | Ressource introuvable |
| `src/main/java/com/bibliotheque/exception/LivreNotFoundException.java` | Livre introuvable |
| `src/main/java/com/bibliotheque/exception/UtilisateurNotFoundException.java` | Utilisateur introuvable (par ID ou email) |
| `src/main/java/com/bibliotheque/exception/EmpruntNotFoundException.java` | Emprunt introuvable |
| `src/main/java/com/bibliotheque/exception/LivreNonDisponibleException.java` | Livre non disponible pour emprunt |
| `src/main/java/com/bibliotheque/exception/EmailDejaUtiliseException.java` | Email deja utilise |
| `src/main/java/com/bibliotheque/exception/EmpruntDejaRetourneException.java` | Emprunt deja retourne |
| `src/main/java/com/bibliotheque/exception/DuplicateResourceException.java` | Ressource en double |

### Services metier

| Fichier | Description |
|---------|-------------|
| `src/main/java/com/bibliotheque/service/LivreService.java` | Gestion des livres avec validation d'unicite |
| `src/main/java/com/bibliotheque/service/UtilisateurService.java` | Gestion des utilisateurs avec validation d'email et normalisation |
| `src/main/java/com/bibliotheque/service/EmpruntService.java` | Gestion des emprunts avec orchestration |

### Methodes disponibles

#### LivreService

| Methode | Type | Description |
|---------|------|-------------|
| `findAll()` | Lecture | Liste tous les livres |
| `findById(id)` | Lecture | Trouve un livre par son ID |
| `findByRecherche(recherche)` | Lecture | Recherche par titre ou auteur |
| `findDisponibles()` | Lecture | Liste les livres disponibles |
| `creer(livre)` | Ecriture | Cree un livre avec validation d'unicite |
| `modifier(id, livre)` | Ecriture | Modifie un livre existant |
| `supprimer(id)` | Ecriture | Supprime un livre (si aucun historique d'emprunt) |
| `estDisponible(id)` | Lecture | Verifie la disponibilite |
| `marquerIndisponible(id)` | Ecriture | Marque comme emprunte |
| `marquerDisponible(id)` | Ecriture | Marque comme disponible |
| `countDisponibles()` | Lecture | Nombre de livres disponibles (COUNT query) |
| `countTotal()` | Lecture | Nombre total de livres |

#### UtilisateurService

| Methode | Type | Description |
|---------|------|-------------|
| `findAll()` | Lecture | Liste tous les utilisateurs |
| `findById(id)` | Lecture | Trouve un utilisateur par son ID |
| `findByEmail(email)` | Lecture | Trouve un utilisateur par email |
| `findByRecherche(recherche)` | Lecture | Recherche par nom ou email |
| `creer(utilisateur)` | Ecriture | Cree un utilisateur avec normalisation + validation d'email |
| `modifier(id, utilisateur)` | Ecriture | Modifie un utilisateur avec normalisation + validation d'email |
| `supprimer(id)` | Ecriture | Supprime un utilisateur (si aucun historique d'emprunt) |
| `emailExiste(email)` | Lecture | Verifie l'existence d'un email |
| `countTotal()` | Lecture | Nombre total d'utilisateurs |

#### EmpruntService

| Methode | Type | Description |
|---------|------|-------------|
| `findAll()` | Lecture | Liste tous les emprunts avec details |
| `findById(id)` | Lecture | Trouve un emprunt par son ID |
| `findActifs()` | Lecture | Liste les emprunts en cours |
| `findHistorique()` | Lecture | Liste les emprunts termines |
| `findByUtilisateur(id)` | Lecture | Emprunts d'un utilisateur |
| `findByLivre(id)` | Lecture | Emprunts d'un livre |
| `findEnRetard()` | Lecture | Emprunts en retard (> 30 jours) |
| `creer(utilisateurId, livreId)` | Ecriture | Cree un emprunt |
| `effectuerRetour(empruntId)` | Ecriture | Enregistre un retour |
| `countActifs()` | Lecture | Nombre d'emprunts en cours (COUNT query) |
| `countEnRetard()` | Lecture | Nombre d'emprunts en retard (COUNT query) |
| `countTotal()` | Lecture | Nombre total d'emprunts |
| `estEnCours(empruntId)` | Lecture | Verifie si un emprunt est actif |

---

## Corrections apres revue de code

Une revue de code par sous-agents a identifie et corrige les points suivants :

### Corrections critiques

| # | Fichier | Probleme | Correction |
|---|---------|----------|------------|
| 1 | `UtilisateurService` | `findByEmail` utilisait `UtilisateurNotFoundException(-1L)` (message trompeur) | Nouveau constructeur `(String email)` dans `UtilisateurNotFoundException` |
| 2 | `UtilisateurService` | `supprimer` ne verifiait pas les emprunts actifs | Ajout initial d'une verification des emprunts associes, ensuite durcie a l'historique complet |
| 3 | `UtilisateurService` | Emails non normalises | Ajout de `normaliserEmail()` (lowercase + trim) |
| 4 | `LivreService` | `validerUniciteTitreAuteur` utilisait LIKE (faux positifs) + NPE sur null | Garde null + filtre exact match apres LIKE |
| 5 | `LivreService` / `UtilisateurService` | `DuplicateResourceException` mal utilise pour les suppressions | Remplacement par `BusinessException("SUPPRESSION_IMPOSSIBLE")` |
| 6 | `LivreService` | `countDisponibles` chargeait tous les entites en memoire | Remplacement par une requete dediee de comptage |
| 7 | `EmpruntService` | `countActifs` chargeait tous les entites en memoire | Utilisation de `countByDateRetourIsNull()` |
| 8 | `EmpruntService` | `countEnRetard` chargeait tous les entites en memoire | Utilisation de `countEmpruntsEnRetard()` |
| 9 | `Emprunt.java` | `getNombreJoursEmprunt` renvoyait faux pour durees > 1 mois | Utilisation de `ChronoUnit.DAYS.between()` |
| 10 | `EmpruntService` | `dateEmprunt` defini deux fois (constructeur + service) | Suppression de la redondance dans le service |
| 11 | `EmpruntRepository` | Methodes COUNT manquantes | Ajout de `countByDateRetourIsNull` et `countEmpruntsEnRetard` |
| 12 | `LivreRepository` | Methode COUNT manquante | Ajout initial d'une methode COUNT, ensuite alignee sur la disponibilite derivee |

### Correctifs complementaires apres revue finale

| # | Fichier | Probleme | Correction |
|---|---------|----------|------------|
| 13 | `UtilisateurService` | `findByEmail` ne normalisait pas l'email en lecture | Normalisation centralisee reappliquee avant recherche et verification d'existence |
| 14 | `LivreService` | `titre` et `auteur` etaient verifies normalises mais sauvegardes bruts | Normalisation appliquee avant validation et persistance |
| 15 | `LivreService` | `supprimer` dependait seulement du flag `disponible` | Verification initiale des emprunts actifs via `EmpruntRepository`, ensuite durcie a l'historique complet |

### Durcissements complementaires

| # | Fichier | Probleme | Correction |
|---|---------|----------|------------|
| 16 | `EmpruntService` / `LivreRepository` | `creer` restait vulnerable aux courses critiques | Chargement pessimiste du livre via `findByIdForUpdate`, plus traduction des violations d'integrite en `LivreNonDisponibleException` |
| 17 | `V3__protect_loan_history_and_enforce_single_active_loan.sql` | L'historique pouvait etre supprime via `ON DELETE CASCADE` | Remplacement par des FK en `ON DELETE RESTRICT` |
| 18 | `V3__protect_loan_history_and_enforce_single_active_loan.sql` | Unicite d'emprunt actif non garantie par `UNIQUE(livre_id, date_retour)` | Ajout d'un index unique partiel sur `livre_id` quand `date_retour IS NULL` |
| 19 | `LivreService` / `LivreRepository` | La disponibilite etait deduite d'un flag pouvant deriver | Disponibilite derivee des emprunts actifs pour lecture, comptage et verification |
| 20 | `LivreService` / `UtilisateurService` | La suppression preservait mal l'historique metier | Suppression interdite des livres/utilisateurs ayant un historique d'emprunts |
| 21 | `Emprunt.java` | Contrainte JPA trompeuse sur `(livre_id, date_retour)` | Suppression de la pseudo-garantie JPA, Flyway devient la source de verite |

### Points mineurs restants (hors scope Phase 4)

| Point | Impact | Traitement prevu |
|-------|--------|-----------------|
| `existsByEmail` est case-sensitive | Faible | Mitige par la normalisation en amont |
| `existsByTitreAndAuteur` non utilise | Faible | Optimisation future possible |
| Pas de limite max d'emprunts par utilisateur | Faible | Regle metier a definir |

---

## Validation realisee

- Compilation Maven reussie avec `mise run build`.
- 24 fichiers sources compiles sans erreur.
- 22 tests unitaires couvrent `LivreService`, `UtilisateurService` et `EmpruntService`.
- 5 tests d'integration PostgreSQL valident Flyway, JPA et les contraintes reelles de persistence.
- Revue de code par 5 sous-agents (LivreService, UtilisateurService, EmpruntService, exceptions, cross-check).
- Toutes les corrections critiques sont compilees et validees.
- Les regles de normalisation email et titre/auteur sont maintenant verifiees par tests.
- La suppression d'un livre ou utilisateur avec historique d'emprunts est maintenant bloquee pour proteger l'historique.
- La creation d'emprunt est maintenant protegee par verrou pessimiste + index unique partiel cote base.
- La disponibilite d'un livre est maintenant alignee sur l'existence d'emprunts actifs pour les lectures metier.
- Les contraintes `ON DELETE RESTRICT` et l'unicite d'emprunt actif sont prouvees sur un vrai PostgreSQL via Testcontainers.
- Les services sont prets a etre consommes par les controllers (Phase 5).

---

## Points reportes

- Les controllers (Phase 5) utiliseront les services crees ici.
- La gestion des erreurs cote controller via `@ControllerAdvice` sera traitee en Phase 8.
- Les messages d'erreur en francais pourront etre externalises si necessaire.
- L'ajout d'une limite metier sur le nombre maximal d'emprunts par utilisateur reste a definir.
- Si le flag `livres.disponible` doit devenir purement derive ou etre supprime du schema, une migration dediee pourra le simplifier davantage.
