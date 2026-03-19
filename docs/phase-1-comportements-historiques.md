# Phase 1 - Comportements historiques a conserver

## Objectif

Lister les comportements utiles de l'application actuelle a garder pendant la migration afin d'eviter une regression fonctionnelle pendant les phases 2 a 6.

## Parcours et routes a conserver temporairement

- `GET /` reste le point d'entree de l'application.
- `GET /livres` affiche la collection complete.
- `GET /livres?action=disponibles` reste disponible tant que la refonte des routes n'est pas engagee.
- `GET /livres?action=recherche&terme=...` reste le mecanisme de recherche courant.
- `GET /livres?action=edit&id=...` reste le point d'entree d'edition avant la phase Spring MVC.
- `GET /utilisateurs` reste la liste principale des membres.
- `GET /utilisateurs?action=edit&id=...` reste le point d'entree d'edition avant migration.
- `GET /emprunts` reste l'ecran operationnel principal.

## Regles metier actuelles a ne pas casser avant refonte

- Un emprunt cree rend le livre indisponible.
- Un retour enregistre rend le livre de nouveau disponible.
- L'ajout et la modification d'utilisateur conservent le controle d'unicite sur l'email via la base.
- Les validations minimales existantes sur livre et utilisateur restent en place jusqu'a leur remplacement par une solution standard.

## Comportements techniques transitoires a conserver

- Les JSP dans `src/main/webapp/WEB-INF/views` restent les vues de reference jusqu'a leur remplacement.
- `CSRFUtil` reste actif tant que Spring Security n'est pas en place.
- Le stockage des messages flash via la session reste acceptable pendant la phase Servlet.
- Le packaging WAR et `web.xml` restent en place jusqu'a la migration du socle.

## Ajustements deja faits en phase 1

- Les suppressions de livres et d'utilisateurs ne passent plus par `GET`.
- Le vocabulaire `retour` remplace `return` dans le module des emprunts.
- Les messages flash sont homogenises avec une utilite commune.
- Les pages d'erreur de base existent desormais aux emplacements declares dans `web.xml`.
