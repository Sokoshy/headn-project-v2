# Phase 0 - Cadrage UX, navigation et architecture d'information

## Objectif

Definir une cible produit claire avant la migration vers Spring Boot afin que les choix techniques servent les parcours metier prioritaires au lieu de reproduire la structure Servlet/JSP actuelle.

## Constats utiles issus de l'existant

- L'accueil agit comme une page de liens rapides, mais pas comme un vrai tableau de bord.
- Le module `Livres` melange ajout, recherche, edition et liste sur un seul ecran.
- Le module `Utilisateurs` ne propose pas de recherche et concentre ajout/edition/liste sur la meme page.
- Le module `Emprunts` couvre deja le coeur metier, mais la distinction entre emprunts actifs et historique reste visuellement secondaire.
- Le vocabulaire et les actions ne sont pas homogenes (`return` / `retour`, `add` / `update`, libelles parfois techniques).

## Parcours utilisateur principaux

### Priorite 1 - Consulter la disponibilite d'un livre

1. Ouvrir `Livres`
2. Rechercher ou filtrer un livre
3. Voir immediatement son statut
4. Decider s'il peut etre emprunte

### Priorite 2 - Enregistrer un emprunt

1. Ouvrir `Emprunts`
2. Selectionner un utilisateur
3. Selectionner un livre disponible
4. Valider l'emprunt
5. Revenir sur une vue confirmee des emprunts actifs

### Priorite 3 - Enregistrer un retour

1. Ouvrir `Emprunts`
2. Identifier un emprunt actif
3. Declencher l'action `Enregistrer le retour`
4. Verifier que le livre redevient disponible

### Priorite 4 - Retrouver un utilisateur

1. Ouvrir `Utilisateurs`
2. Rechercher par nom ou email
3. Consulter les informations utiles
4. Acceder a la modification si necessaire

### Priorite 5 - Consulter l'historique des emprunts

1. Ouvrir `Emprunts`
2. Basculer vers l'historique ou le faire apparaitre sous les actifs
3. Filtrer ou parcourir les retours termines

### Priorite 6 - Administrer le catalogue et les membres

1. Ajouter ou modifier un livre
2. Ajouter ou modifier un utilisateur
3. Supprimer avec confirmation explicite uniquement en action secondaire

## Priorisation des parcours

| Priorite | Parcours | Frequence attendue | Valeur metier |
| --- | --- | --- | --- |
| P1 | Consulter les livres et leur disponibilite | Tres forte | Critique |
| P1 | Enregistrer un emprunt | Tres forte | Critique |
| P1 | Enregistrer un retour | Tres forte | Critique |
| P2 | Retrouver un utilisateur | Forte | Haute |
| P2 | Consulter l'historique | Moyenne | Haute |
| P3 | Ajouter / modifier livres et utilisateurs | Moyenne | Moyenne |
| P3 | Supprimer un livre ou un utilisateur | Faible | Sensible |

## Navigation principale cible

Navigation persistante de premier niveau :

- `Accueil`
- `Livres`
- `Utilisateurs`
- `Emprunts`

Regles de navigation :

- `Accueil` sert de tableau de bord et de point d'entree, pas de page marketing.
- Chaque module expose une page principale unique, lisible sans action prealable.
- Les actions de creation restent visibles, mais ne dominent pas la consultation des donnees.
- Les actions destructives ne figurent jamais dans la navigation principale.

## Role exact de chaque ecran

### `Accueil`

- Donner une vue d'ensemble immediate de l'activite.
- Mettre en avant les actions frequentes : voir les livres, creer un emprunt, enregistrer un retour.
- Afficher quelques indicateurs utiles : livres disponibles, emprunts actifs, retours du jour si disponible plus tard.

### `Livres`

- Ecran de consultation du catalogue en priorite.
- Recherche et filtres visibles au-dessus de la liste.
- Disponibilite lisible au premier coup d'oeil.
- Creation et edition accessibles comme actions secondaires bien identifiees.

### `Utilisateurs`

- Ecran de recherche et consultation des membres.
- Mise en avant du nom, de l'email et du statut utile a l'emprunt.
- Creation et edition accessibles sans melanger l'ecran a un formulaire envahissant.

### `Emprunts`

- Ecran centre sur l'operationnel.
- Le bloc `Nouvel emprunt` doit etre rapide a utiliser.
- Les `Emprunts actifs` sont prioritaires sur l'historique.
- L'action de retour doit etre visible, directe et sans ambiguite.

## Contenus prioritaires vs secondaires

### Accueil

- Prioritaires : acces rapides, indicateurs, emprunts actifs, livres disponibles.
- Secondaires : texte de presentation, informations institutionnelles, details decoratifs.

### Livres

- Prioritaires : recherche, filtres, tableau, disponibilite, action d'edition.
- Secondaires : formulaire de creation long, informations systeme, identifiants techniques trop visibles.

### Utilisateurs

- Prioritaires : recherche, liste, nom, email, action d'edition.
- Secondaires : date d'inscription si elle n'aide pas l'action courante, suppression trop mise en avant.

### Emprunts

- Prioritaires : formulaire de nouvel emprunt, emprunts actifs, action de retour, statuts.
- Secondaires : historique complet toujours ouvert, informations redondantes, details techniques de base.

## Structure cible par module

### Module `Accueil`

1. Bandeau titre + resume
2. Cartes indicateurs
3. Actions rapides
4. Apercu operationnel

### Module `Livres`

1. En-tete de page + action `Ajouter un livre`
2. Zone recherche / filtres
3. Tableau principal
4. Etat vide ou resultat de recherche
5. Formulaire de creation / edition en page dediee ou panneau secondaire selon implementation retenue en phase UI

### Module `Utilisateurs`

1. En-tete de page + action `Ajouter un utilisateur`
2. Zone recherche
3. Tableau principal
4. Etat vide
5. Formulaire de creation / edition sur ecran dedie ou panneau secondaire

### Module `Emprunts`

1. En-tete de page + resume des actifs
2. Bloc `Nouvel emprunt`
3. Liste des emprunts actifs
4. Historique recent ou section historique pliante
5. Filtres d'historique si necessaire

## Hierarchie visuelle commune

- Niveau 1 : titre de page + benefice principal de la page.
- Niveau 2 : bloc d'action principal ou bloc de pilotage.
- Niveau 3 : contenu de consultation principal (tableau, liste, indicateurs).
- Niveau 4 : actions secondaires comme edition, export futur, suppression.
- Les messages de succes ou d'erreur apparaissent juste sous le header de page.
- Les statuts doivent etre codes de facon constante : disponible, emprunte, retourne.

## Conventions de vocabulaire dans l'interface

Utiliser partout des verbes metier et des libelles coherents :

- `Accueil`
- `Livres`
- `Utilisateurs`
- `Emprunts`
- `Ajouter un livre`
- `Modifier`
- `Supprimer`
- `Nouvel emprunt`
- `Enregistrer l'emprunt`
- `Enregistrer le retour`
- `Disponible`
- `Emprunte`
- `Retourne`
- `Historique des emprunts`

Conventions complementaires :

- Bannir `return` dans l'interface ; utiliser `retour` ou `enregistrer le retour`.
- Bannir les labels purement techniques comme `add`, `update`, `delete` hors couche code.
- Preferer `utilisateur` dans l'interface et garder `membre` seulement comme texte d'accompagnement si utile.
- Preferer des messages orientes action : `Livre ajoute`, `Utilisateur modifie`, `Retour enregistre`.

## Alignement UX sur les besoins metier reels

- Le besoin central n'est pas d'editer des donnees, mais de gerer rapidement la circulation des livres.
- Les ecrans doivent donc prioriser consultation, disponibilite et traitement des emprunts.
- Les formulaires d'administration restent necessaires, mais passent derriere l'usage quotidien.
- Le module `Emprunts` devient le centre operationnel du produit.
- Le module `Livres` devient le centre de consultation du catalogue.
- Le module `Utilisateurs` devient un outil de recherche et de gestion de fiches membres, pas un simple CRUD brut.

## Decisions retenues pour la suite

- La navigation cible est validee autour de `Accueil`, `Livres`, `Utilisateurs`, `Emprunts`.
- Les parcours prioritaires de la migration sont : consulter disponibilite, emprunter, retourner, retrouver un utilisateur.
- Le role de chaque ecran est explicite et servira de base a la phase 5 pour les routes puis a la phase 6 pour la refonte UI.
- Les formulaires de creation / edition ne doivent plus structurer a eux seuls les pages principales.
