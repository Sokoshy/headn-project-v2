# Phase 2 - Migration du socle vers Spring Boot

## Etat valide

La phase 2 est validee avec un socle Spring Boot demarrable, un build Maven coherent et une configuration exploitable localement.

## Decisions techniques appliquees

- application Spring Boot creee avec `@SpringBootApplication`
- packaging Maven en `jar`
- configuration centralisee dans `application.yml`
- integration de PostgreSQL, JPA, Thymeleaf, Validation, Security, Test et Flyway
- suppression des mecanismes historiques devenus inutiles (`web.xml`, bootstrap manuel, configuration Cargo/Jetty)
- ajout d'un controller Spring MVC minimal pour exposer la page d'accueil
- ajout d'une configuration Spring Security minimale autorisant l'acces a l'application pendant la transition

## Java cible

- version projet retenue : `Java 25`

Le projet compile et demarre proprement avec `Java 25`, qui devient la base de reference pour la suite du refactoring.
La migration vers `Java 26` est reportee volontairement afin de privilegier la stabilite du build et la reproductibilite locale.

## Verifications effectuees

- `mvn clean test` : succes
- `mvn spring-boot:run` : succes
- verification HTTP sur `/bibliotheque/` : reponse `200 OK`

## Nettoyages effectues

- suppression des fichiers `.class` presents dans `src/main/java`
- suppression des anciennes pages `src/main/webapp` devenues inutiles pour le socle Spring Boot valide

## Points assumes pour la suite

- la securite est presente via Spring Security mais reste volontairement minimale a ce stade
- la refonte de la persistence JPA detaillee reste traitee en phase 3
- la refonte complete des controllers et des parcours web reste traitee en phase 5
