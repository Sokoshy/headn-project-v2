# Phase 3 — Refonte de la persistence

## Objectif

Remplacer les classes modele historiques par des entites JPA standardisees et creer les repositories Spring Data correspondants.

---

## Decisions prises

### Choix des types d'identifiants

- Les champs `id` des entites utilisent `Long` au lieu de `int`.
- Justification : `BIGSERIAL` en PostgreSQL correspond a `Long` en Java.
- L'ancien code utilisait `int`, ce qui etait incoherent avec le type SQL.

### Strategie de generation des identifiants

- `GenerationType.IDENTITY` retenu pour les trois entites.
- Coherent avec le mecanisme `BIGSERIAL` de PostgreSQL.

### Configuration Hibernate

- `ddl-auto: validate` maintenu dans `application.yml`.
- Hibernate valide la correspondance schema / entites sans generer de DDL.
- Flyway reste la source de verite pour les evolutions de schema.

### Relations entre entites

- `Emprunt.utilisateur` → `@ManyToOne(fetch = LAZY)` vers `Utilisateur`
- `Emprunt.livre` → `@ManyToOne(fetch = LAZY)` vers `Livre`
- Les champs historiques `nomUtilisateur`, `emailUtilisateur`, `titreLivre`, `auteurLivre` ont ete supprimes de l'entite `Emprunt`.
- Ces informations sont accessibles via les relations JPA (`emprunt.getUtilisateur().getNom()`).

### Contraintes de validation

- Annotations `jakarta.validation` ajoutees sur les entites.
- `@NotBlank`, `@Size`, `@Email`, `@NotNull` selon le contexte.
- Les messages de validation sont en francais pour rester coherents avec l'interface.

### Migration Flyway

- `V1__initial_schema.sql` reste la baseline fonctionnelle du schema.
- Une migration corrective `V2__align_schema_with_jpa.sql` a ete ajoutee pour aligner une base historique existante avec les mappings JPA.
- La migration V2 convertit les identifiants et cles etrangeres en `BIGINT`, force les colonnes requises en `NOT NULL` et recree les vues historiques dependantes.
- Une configuration explicite de Flyway a ete ajoutee pour garantir l'execution des migrations avant l'initialisation JPA.

---

## Changements effectues

### Entites JPA

| Fichier | Description |
|---------|-------------|
| `src/main/java/com/bibliotheque/model/Livre.java` | Entite JPA `@Table(name = "livres")` avec validation |
| `src/main/java/com/bibliotheque/model/Utilisateur.java` | Entite JPA `@Table(name = "utilisateurs")` avec validation |
| `src/main/java/com/bibliotheque/model/Emprunt.java` | Entite JPA `@Table(name = "emprunts")` avec relations `@ManyToOne` |

### Repositories Spring Data

| Fichier | Description |
|---------|-------------|
| `src/main/java/com/bibliotheque/repository/LivreRepository.java` | Recherches par titre, auteur, disponibilite |
| `src/main/java/com/bibliotheque/repository/UtilisateurRepository.java` | Recherche par email, nom |
| `src/main/java/com/bibliotheque/repository/EmpruntRepository.java` | Requetes metier avec `JOIN FETCH` |

### Infrastructure Flyway

| Fichier | Description |
|---------|-------------|
| `src/main/java/com/bibliotheque/config/FlywayConfig.java` | Declenche Flyway avant `entityManagerFactory` |
| `src/main/resources/db/migration/V1__initial_schema.sql` | Baseline initiale du schema |
| `src/main/resources/db/migration/V2__align_schema_with_jpa.sql` | Migration corrective d'alignement schema / JPA |

### Requetes metier definies dans `EmpruntRepository`

| Methode | Usage |
|---------|-------|
| `findEmpruntsEnRetard(date)` | Emprunts non rendus depassant 30 jours |
| `findAllWithDetails()` | Tous les emprunts avec utilisateur et livre charges |
| `findEmpruntsActifs()` | Emprunts en cours (dateRetour IS NULL) |
| `findHistorique()` | Emprunts termines (dateRetour IS NOT NULL) |

### Suppressions

- Les champs denormalises de `Emprunt` (`nomUtilisateur`, `emailUtilisateur`, `titreLivre`, `auteurLivre`) ont ete supprimes.
- Les constructeurs historiques avec `int id` ont ete supprimes.
- Les constructeurs avec plusieurs signatures redondantes ont ete simplifies.

---

## Alignement schema / entites

| Table SQL | Entite JPA | Mapping |
|-----------|------------|---------|
| `livres.id` BIGSERIAL | `Livre.id` Long | `@Id @GeneratedValue(IDENTITY)` |
| `livres.titre` VARCHAR(255) NOT NULL | `Livre.titre` String | `@Column(nullable=false)` |
| `livres.auteur` VARCHAR(255) NOT NULL | `Livre.auteur` String | `@Column(nullable=false)` |
| `livres.disponible` BOOLEAN NOT NULL DEFAULT TRUE | `Livre.disponible` boolean | `@Column(nullable=false)` |
| `livres.date_creation` TIMESTAMP | `Livre.dateCreation` LocalDateTime | `@Column(name="date_creation", updatable=false)` |
| `utilisateurs.id` BIGSERIAL | `Utilisateur.id` Long | `@Id @GeneratedValue(IDENTITY)` |
| `utilisateurs.nom` VARCHAR(255) NOT NULL | `Utilisateur.nom` String | `@Column(nullable=false)` |
| `utilisateurs.email` VARCHAR(255) UNIQUE NOT NULL | `Utilisateur.email` String | `@Column(nullable=false, unique=true)` |
| `utilisateurs.date_inscription` TIMESTAMP | `Utilisateur.dateInscription` LocalDateTime | `@Column(name="date_inscription", updatable=false)` |
| `emprunts.id` BIGSERIAL | `Emprunt.id` Long | `@Id @GeneratedValue(IDENTITY)` |
| `emprunts.utilisateur_id` BIGINT NOT NULL FK | `Emprunt.utilisateur` Utilisateur | `@ManyToOne @JoinColumn(name="utilisateur_id", nullable=false)` |
| `emprunts.livre_id` BIGINT NOT NULL FK | `Emprunt.livre` Livre | `@ManyToOne @JoinColumn(name="livre_id", nullable=false)` |
| `emprunts.date_emprunt` DATE | `Emprunt.dateEmprunt` LocalDate | `@Column(name="date_emprunt")` |
| `emprunts.date_retour` DATE | `Emprunt.dateRetour` LocalDate | `@Column(name="date_retour")` |

---

## Validation realisee

- Compilation Maven reussie avec `mise run build`.
- 12 fichiers sources compiles sans erreur.
- Demarrage valide avec `mvn spring-boot:run` via `mise` sur une base PostgreSQL locale apres application des migrations.
- Demarrage complet revalide sur un port alternatif (`8081`) pour contourner un port `8080` deja occupe localement.
- Hibernate valide desormais correctement la correspondance schema / entites avec `ddl-auto: validate`.
- Flyway cree et maintient `flyway_schema_history` avec les versions `1` et `2` appliquees avec succes.
- Packaging JAR Spring Boot fonctionnel.

---

## Points reportes

- La migration vers `Java 26` est toujours reportee.
- Les services metier (Phase 4) utiliseront directement les repositories crees ici.
- Les controllers (Phase 5) consommeront les repositories via les services.
- La gestion des cas d'erreur (ex : emprunt d'un livre non disponible) sera traitee en Phase 4.
