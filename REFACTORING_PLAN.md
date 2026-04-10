# REFACTORING PLAN - Gestionnaire de Bibliotheque

## 1. Contexte

### 1.1 Etat actuel

L'application actuelle est basee sur :

- Java Servlet / JSP
- PostgreSQL
- Maven
- deploiement sur conteneur Servlet

Constats principaux :

- dette technique importante
- architecture peu modulaire
- logique web, logique metier et acces aux donnees trop proches
- validation et securite heterogenes
- navigation peu claire
- interface trop proche d'un CRUD technique

### 1.2 Objectif du refactoring

Le refactoring poursuit deux objectifs lies :

- refondre completement l'UI/UX et la navigation
- moderniser le socle technique pour soutenir cette refonte

La priorite est la suivante :

1. simplifier l'experience utilisateur
2. clarifier la navigation
3. reorganiser l'application autour des parcours metier
4. moderniser la technique sans perdre en simplicite

---

## 2. Stack cible

### 2.1 Stack retenue

- Java 25
- Maven 3.9
- Spring Boot 4.0.4
- Spring MVC
- Thymeleaf
- Spring Data JPA
- Spring Security
- PostgreSQL
- Flyway

### 2.2 Decision de version Java

- version projet retenue : `Java 25`
- la montee vers `Java 26` est reportee volontairement
- le projet privilegie pour l'instant la stabilite de build et la reproductibilite locale

### 2.3 Contraintes validees

- Spring Boot 4.0.4 est compatible avec Java 25
- Maven 3.9 est compatible avec Spring Boot 4.0.4

---

## 3. Vision UX cible

### 3.1 Objectif produit

L'application doit devenir :

- plus simple a comprendre
- plus ordonnee
- plus lisible
- plus rapide a utiliser
- plus coherente dans tous les modules

### 3.2 Navigation principale cible

La navigation principale doit etre :

- Accueil
- Livres
- Utilisateurs
- Emprunts

### 3.3 Parcours prioritaires

Les parcours a optimiser en priorite sont :

- consulter les livres
- verifier leur disponibilite
- retrouver un utilisateur
- enregistrer un emprunt
- enregistrer un retour
- consulter l'historique des emprunts

---

## 4. Architecture cible

### 4.1 Architecture fonctionnelle

L'application doit etre reorganisee autour de modules clairs :

- `Accueil`
- `Livres`
- `Utilisateurs`
- `Emprunts`

### 4.2 Architecture technique

Separation cible :

- `controller`
- `service`
- `repository`
- `entity`
- `dto` / `form` si necessaire
- `templates`
- `config`

### 4.3 Principes de migration

- ne pas recopier l'existant tel quel
- corriger les incoherences avant de migrer
- penser d'abord parcours utilisateur, ensuite implementation
- supprimer progressivement l'ancien socle seulement apres validation

---

## 5. Structure cible des pages

### 5.1 Accueil

La page d'accueil doit fournir :

- une vue d'ensemble
- des cartes d'acces rapide
- des statistiques utiles
- des raccourcis vers les actions frequentes

### 5.2 Livres

La page Livres doit inclure :

- recherche visible
- filtres simples
- tableau lisible
- disponibilite visible immediatement
- actions claires

### 5.3 Utilisateurs

La page Utilisateurs doit inclure :

- liste claire
- recherche simple
- ajout / modification faciles
- lecture rapide des informations utiles

### 5.4 Emprunts

La page Emprunts doit mettre en avant :

- nouvel emprunt
- emprunts actifs
- action de retour
- historique
- statuts lisibles

---

## 6. Audit de l'existant

### 6.1 Problemes fonctionnels identifies

- formulaire d'edition des livres incoherent
- logique de modification de livre mal structuree
- suppressions possibles via GET
- incoherence `return` / `retour`
- couche service contournee dans certains cas
- messages utilisateur non homogenes
- references d'erreur non resolues

### 6.2 Problemes UX identifies

- navigation peu structuree
- manque de repere global
- ecrans trop techniques
- hierarchie visuelle insuffisante
- actions prioritaires mal mises en avant
- manque de coherence inter-pages

### 6.3 Problemes techniques identifies

- dependances Maven a revoir
- `web.xml` ancien ou incoherent
- chemin absolu Windows dans `pom.xml`
- fichiers `.class` dans le source tree
- absence de tests
- architecture vieillissante et peu evolutive

---

## 7. Plan de refactoring detaille

### Regle de livrable par phase

- Chaque phase doit se conclure par un livrable technique ecrit.
- Ce livrable doit etre ajoute dans le dossier `docs`.
- Le nommage doit suivre la structure deja utilisee par les documents existants, sous la forme `docs/phase-X-<intitule>.md`.
- Le livrable doit resumer les decisions prises, les changements effectues, les validations realisees et les points reportes a la phase suivante.

### Phase 0 - Cadrage UX, navigation et architecture d'information

### Objectif

Definir la cible produit, la logique de navigation et les parcours avant la migration technique.

Livrable produit : `docs/phase-0-cadrage-ux.md`

### Checklist

- [x] Identifier tous les parcours utilisateur principaux
- [x] Prioriser les parcours les plus frequents
- [x] Definir la navigation principale cible
- [x] Definir le role exact de chaque page
- [x] Distinguer les contenus prioritaires des contenus secondaires
- [x] Decider de la structure cible de chaque module
- [x] Definir une hierarchie visuelle commune
- [x] Definir les conventions de vocabulaire dans l'interface
- [x] Aligner la structure UX sur les besoins metier reels

### Critere de sortie

- [x] La navigation cible est clairement definie
- [x] Les parcours principaux sont formalises
- [x] Le role de chaque ecran est explicite

---

### Phase 1 - Stabilisation de l'existant

### Objectif

Corriger les incoherences critiques pour simplifier la migration.

Livrable produit : `docs/phase-1-comportements-historiques.md`

### Checklist

- [x] Corriger le formulaire d'edition des livres
- [x] Corriger l'incoherence `return` / `retour`
- [x] Remplacer les actions destructives via GET
- [x] Homogeneiser temporairement les messages flash
- [x] Corriger les references de pages d'erreur invalides
- [x] Nettoyer les fichiers `.class` presents dans `src`
- [x] Corriger le chemin absolu Windows dans le `pom.xml`
- [x] Revoir les dependances minimales necessaires au build actuel
- [x] Documenter les comportements historiques a conserver

### Critere de sortie

- [x] Les incoherences les plus risquees sont corrigees
- [x] L'existant est plus stable et plus lisible
- [x] Le point de depart de migration est assaini

---

### Phase 2 - Migration du socle vers Spring Boot

### Objectif

Poser un nouveau socle applicatif moderne, standard et maintenable.

### Checklist

- [x] Creer l'application Spring Boot
- [x] Ajouter `@SpringBootApplication`
- [x] Configurer le packaging Maven
- [x] Creer `application.yml` en remplacement de `config.properties` et d'une partie de `DatabaseConfig.java`
- [x] Configurer l'environnement PostgreSQL
- [x] Ajouter `spring-boot-starter-web`
- [x] Ajouter `spring-boot-starter-thymeleaf`
- [x] Ajouter `spring-boot-starter-data-jpa`
- [x] Ajouter `spring-boot-starter-validation`
- [x] Ajouter `spring-boot-starter-security`
- [x] Ajouter `spring-boot-starter-test`
- [x] Ajouter le driver PostgreSQL
- [x] Ajouter Flyway
- [x] Supprimer les mecanismes d'initialisation historiques devenus inutiles (`web.xml`, configuration WAR/Cargo/Jetty, bootstrap manuel), puis retirer `CSRFUtil.java` seulement une fois Spring Security en place
- [x] Aligner `mise.toml` sur la nouvelle stack
- [x] Configurer `Java 25`
- [x] Reporter explicitement la migration vers `Java 26`

### Critere de sortie

- [x] L'application demarre sous Spring Boot
- [x] Le build Maven est coherent
- [x] La stack cible est en place

### Note d'implementation

- Le projet est aligne integralement sur `Java 25` pour garantir un build propre, un demarrage valide et une execution reproductible dans l'environnement actuel.
- La migration vers `Java 26` est reportee volontairement a une phase ulterieure, une fois l'environnement et la chaine de build completement stabilises.

Livrable technique : `docs/phase-2-socle-spring-boot.md`

---

### Phase 3 - Refonte de la persistence

### Objectif

Remplacer les DAO manuels par une couche persistence standardisee.

Livrable technique : `docs/phase-3-refonte-persistence.md`

### Checklist

- [x] Convertir `Livre` en entite JPA
- [x] Convertir `Utilisateur` en entite JPA
- [x] Convertir `Emprunt` en entite JPA
- [x] Definir les relations entre entites
- [x] Creer les repositories Spring Data
- [x] Definir les contraintes de persistence
- [x] Identifier les requetes metier specifiques
- [x] Migrer progressivement les acces DAO vers les repositories
- [x] Supprimer les DAO devenus inutiles
- [x] Integrer Flyway
- [x] Creer les scripts SQL versionnes
- [x] Verifier l'alignement schema / entites
- [x] Encadrer la migration du schema sans dependre d'un `ddl-auto` implicite, en faisant de Flyway la source de verite pour les evolutions SQL

### Critere de sortie

- [x] La persistence passe par JPA / repositories
- [x] Le schema est maitrise via migrations
- [x] Les DAO historiques peuvent etre retires progressivement

---

### Phase 4 - Refonte de la logique metier

### Objectif

Centraliser toutes les regles metier dans des services clairs.

Livrable technique : `docs/phase-4-refonte-logique-metier.md`

### Checklist

- [x] Refactoriser `LivreService`
- [x] Refactoriser `UtilisateurService`
- [x] Creer ou normaliser `EmpruntService`
- [x] Sortir la logique metier des controllers
- [x] Interdire le contournement direct de la couche service
- [x] Revoir les validations metier de creation
- [x] Revoir les validations metier de modification
- [x] Encadrer la disponibilite d'un livre
- [x] Encadrer l'unicite de l'email utilisateur
- [x] Encadrer la creation d'emprunt
- [x] Encadrer le retour d'emprunt
- [x] Gérer la mise a jour de disponibilite
- [x] Introduire des exceptions metier si necessaire
- [x] Standardiser les messages metier retournes aux controllers

### Critere de sortie

- [x] Les services portent toutes les regles metier importantes
- [x] Les controllers ne contiennent plus de logique metier significative
- [x] Les traitements sont testables de facon unitaire

---

### Phase 5 - Refactoring des controllers et des routes

### Objectif

Construire une couche web claire, coherent et alignee sur les usages.

Livrable technique : `docs/phase-5-controllers-routes.md`

### Checklist

- [x] Remplacer progressivement les Servlets par des controllers Spring MVC
- [x] Definir les routes principales de l'application
- [x] Implementer `GET /`
- [x] Implementer `GET /livres`
- [x] Implementer `GET /livres/nouveau` si retenu
- [x] Implementer `GET /livres/{id}/edit`
- [x] Implementer `POST /livres`
- [x] Implementer `POST /livres/{id}`
- [x] Implementer `POST /livres/{id}/delete`
- [x] Aligner les routes `utilisateurs` sur les memes conventions
- [x] Implementer `GET /emprunts`
- [x] Implementer `POST /emprunts`
- [x] Implementer `POST /emprunts/{id}/retour`
- [x] Utiliser proprement les redirections et flash attributes en remplacement du mecanisme actuel base sur `HttpSession` dans les Servlets
- [x] Normaliser les noms d'action et le vocabulaire expose
- [x] Uniformiser la gestion des erreurs de formulaire

### Critere de sortie

- [x] Les routes sont lisibles et coherentes
- [x] La couche web est modernisee
- [x] Les parcours sont mieux supportes par la structure HTTP

---

### Phase 6 - Refonte complete UI/UX

### Objectif

Refondre les pages et composants pour obtenir une experience simple, ordonnee et professionnelle.

Livrable technique : `docs/phase-6-refonte-ui-ux.md`

### Checklist layout global

- [x] Creer un layout global commun
- [x] Integrer une navigation principale persistante
- [x] Definir une structure de page commune
- [x] Standardiser les titres et sous-titres
- [x] Standardiser les zones d'actions
- [x] Standardiser les zones de messages flash

### Checklist composants

- [x] Standardiser les boutons
- [x] Standardiser les formulaires
- [x] Standardiser les champs d'erreur
- [x] Standardiser les tableaux
- [x] Standardiser les badges de statut
- [x] Standardiser les cartes
- [x] Standardiser les confirmations d'action
- [x] Introduire des etats vides
- [x] Introduire des composants de filtre et recherche

### Checklist page Accueil

- [x] Ajouter une vue d'ensemble utile
- [x] Ajouter des cartes d'acces rapide
- [x] Ajouter des statistiques pertinentes
- [x] Ajouter des raccourcis vers les actions frequentes
- [x] Mettre en avant les modules principaux

### Checklist page Livres

- [x] Afficher une recherche visible
- [x] Ajouter des filtres simples
- [x] Rendre la disponibilite immediatement lisible
- [x] Clarifier les actions de creation et edition
- [x] Clarifier la suppression
- [x] Rendre le tableau facile a parcourir

### Checklist page Utilisateurs

- [x] Rendre la liste plus lisible
- [x] Ajouter une recherche simple
- [x] Clarifier l'ajout
- [x] Clarifier la modification
- [x] Mettre en avant les informations utiles sans surcharge

### Checklist page Emprunts

- [x] Donner la priorite aux emprunts actifs
- [x] Clarifier le bloc de nouvel emprunt
- [x] Rendre l'action de retour evidente
- [x] Organiser clairement l'historique
- [x] Afficher des statuts lisibles

### Critere de sortie

- [x] L'application est visuellement coherente
- [x] La navigation est plus simple
- [x] Les actions principales sont visibles rapidement
- [x] Les ecrans sont plus ordonnes

---

### Phase 7 - Responsive et accessibilite

### Objectif

Garantir la qualite d'usage sur tous les formats et ameliorer l'accessibilite.

Livrable technique : `docs/phase-7-responsive-accessibilite.md`

### Checklist

- [x] Adopter une approche mobile first
- [x] Definir des breakpoints clairs
- [x] Adapter les tableaux au mobile
- [x] Adapter les formulaires au mobile
- [x] Garantir un focus visible
- [x] Verifier les contrastes
- [x] Verifier la navigation clavier
- [x] Associer correctement labels et champs
- [x] Assurer une structure semantique correcte
- [x] Rendre les messages d'erreur comprehensibles

### Critere de sortie

- [x] L'interface reste lisible sur mobile
- [x] L'application est utilisable au clavier
- [x] Le niveau d'accessibilite est nettement ameliore

---

### Phase 8 - Securite et robustesse

### Objectif

Fiabiliser la securite et la gestion globale des erreurs.

Livrable technique : `docs/phase-8-securite-robustesse.md`

### Checklist

- [ ] Integrer Spring Security
- [ ] Activer la protection CSRF
- [ ] Securiser les actions destructives en POST
- [ ] Mettre en place `@ControllerAdvice`
- [ ] Introduire `@Valid`
- [ ] Traiter `BindingResult`
- [ ] Creer des pages 404 propres
- [ ] Creer des pages 500 propres
- [ ] Uniformiser les messages techniques et fonctionnels
- [ ] Verifier qu'aucune protection historique n'est retiree trop tot, notamment pendant la transition depuis `CSRFUtil.java` et les controles partiels actuels

### Critere de sortie

- [ ] La securite repose sur des mecanismes standards
- [ ] Les erreurs sont gerees proprement
- [ ] Les parcours critiques sont plus robustes

---

### Phase 9 - Tests, validation et nettoyage final

### Objectif

Valider la qualite finale et retirer proprement l'ancien socle.

Livrable technique : `docs/phase-9-tests-validation-nettoyage.md`

### Checklist tests

- [ ] Ajouter des tests unitaires de services
- [ ] Ajouter des tests de repositories
- [ ] Ajouter des tests de controllers
- [ ] Tester les parcours critiques
- [ ] Tester les cas d'erreur importants

### Checklist validation

- [ ] Verifier `mise run build`
- [ ] Verifier `mise run test`
- [ ] Verifier `mise run dev`
- [ ] Verifier le demarrage applicatif
- [ ] Verifier les migrations Flyway
- [ ] Verifier les routes principales
- [ ] Verifier les parcours metier principaux

### Checklist nettoyage

- [ ] Supprimer les anciens Servlets devenus inutiles
- [ ] Supprimer les JSP obsoletes uniquement une fois toutes les vues Thymeleaf equivalentes validees
- [ ] Supprimer les DAO historiques
- [ ] Supprimer les configurations mortes
- [ ] Verifier `.gitignore`
- [ ] Nettoyer les artefacts residuels

### Critere de sortie

- [ ] Le projet est propre
- [ ] Le socle historique peut etre retire
- [ ] La cible UX et technique est atteinte

---

## 8. Design system cible

### 8.1 Direction visuelle

Le design doit etre :

- moderne
- sobre
- lisible
- professionnel
- efficace

### 8.2 Checklist design system

- [ ] Definir une palette stable
- [ ] Definir une echelle d'espacements
- [ ] Definir les styles de boutons
- [ ] Definir les styles de formulaires
- [ ] Definir les styles de tableaux
- [ ] Definir les badges de statut
- [ ] Definir les styles des messages
- [ ] Definir les etats vides
- [ ] Definir les breakpoints responsive
- [ ] Centraliser les tokens UI

---

## 9. Configuration cible

### Checklist environnement

- [x] Fixer `Java 25` comme cible du projet
- [x] Reporter l'upgrade vers `Java 26`
- [x] Fixer `Maven 3.9`
- [x] Fixer `Spring Boot 4.0.4`
- [x] Utiliser PostgreSQL
- [x] Utiliser Flyway pour les migrations

### Checklist `mise.toml`

- [x] `java = "25"`
- [x] `maven = "3.9"`
- [x] `dev -> mvn spring-boot:run` (remplace `mvn jetty:run`)
- [x] `build -> mvn clean package`
- [x] `start -> mvn spring-boot:run` (remplace `mvn cargo:run`)
- [x] `test -> mvn test`
- [x] `test:all -> mvn verify`
- [x] `db -> docker compose up -d`
- [x] `db:stop -> docker compose down`

---

## 10. Criteres globaux de validation

### Checklist finale

- [ ] La navigation principale est evidente
- [ ] Les modules sont mieux organises
- [ ] Les pages sont coherentes entre elles
- [ ] Les actions importantes sont accessibles rapidement
- [ ] Les parcours d'emprunt et de retour sont fluides
- [ ] Les formulaires sont homogenes
- [ ] Les messages de succes et d'erreur sont clairs
- [ ] L'application fonctionne sur desktop et mobile
- [ ] Le projet demarre sous Spring Boot
- [ ] La persistence est migree proprement
- [ ] Les tests critiques passent
- [ ] L'ancien socle Servlet / JSP peut etre retire proprement
