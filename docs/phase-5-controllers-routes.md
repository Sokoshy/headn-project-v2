# Phase 5 - Refactoring des Controllers et des Routes

## 1. Contexte

Cette phase marque la transition complete de la couche web depuis les Servlets historiques vers les controllers Spring MVC. L'objectif est de construire une architecture web moderne, coherente et alignee sur les parcours utilisateur definis en Phase 0.

## 2. Decisions prises

### 2.1 Routes principales

Les routes ont ete definies selon une convention REST-like adaptee aux besoins metier :

| Methode | Route | Description |
|---------|-------|-------------|
| GET | `/` | Page d'accueil avec statistiques |
| GET | `/livres` | Liste des livres avec recherche |
| GET | `/livres/nouveau` | Formulaire de creation |
| GET | `/livres/{id}` | Detail d'un livre |
| GET | `/livres/{id}/edition` | Formulaire de modification |
| POST | `/livres` | Creation d'un livre |
| POST | `/livres/{id}` | Modification d'un livre |
| POST | `/livres/{id}/suppression` | Suppression d'un livre |
| GET | `/utilisateurs` | Liste des utilisateurs |
| GET | `/utilisateurs/nouveau` | Formulaire de creation |
| GET | `/utilisateurs/{id}` | Detail d'un utilisateur |
| GET | `/utilisateurs/{id}/edition` | Formulaire de modification |
| POST | `/utilisateurs` | Creation d'un utilisateur |
| POST | `/utilisateurs/{id}` | Modification d'un utilisateur |
| POST | `/utilisateurs/{id}/suppression` | Suppression d'un utilisateur |
| GET | `/emprunts` | Liste des emprunts (actifs + historique) |
| GET | `/emprunts/{id}` | Detail d'un emprunt |
| POST | `/emprunts` | Creation d'un emprunt |
| POST | `/emprunts/{id}/retour` | Enregistrement du retour |

### 2.2 Conventions adoptees

- **Creation** : `GET /{resource}/nouveau` (formulaire) + `POST /{resource}` (traitement)
- **Modification** : `GET /{resource}/{id}/edition` (formulaire) + `POST /{resource}/{id}` (traitement)
- **Suppression** : `POST /{resource}/{id}/suppression` (action destructive en POST uniquement)
- **Detail** : `GET /{resource}/{id}` (lecture seule)
- **Liste** : `GET /{resource}` (avec parametres de recherche eventuels)

### 2.3 Gestion des messages

- Utilisation des **Flash Attributes** pour les messages de succes et d'erreur
- Les messages persistent pour une seule requete (affichage apres redirection)
- Convention de nommage : `success` et `error`

### 2.4 Structure des controllers

Chaque controller suit le pattern :
1. Injection des services necessaires via le constructeur
2. Methodes de lecture (GET) sans effet de bord
3. Methodes d'ecriture (POST) avec validation et redirection

## 3. Controllers implementes

### 3.1 HomeController

- `GET /` : Affiche la page d'accueil avec les statistiques globales
- Livrable : nombre de livres, livres disponibles, utilisateurs, emprunts actifs, emprunts en retard

### 3.2 LivreController

- Liste avec recherche par titre ou auteur
- Creation avec validation
- Modification avec validation
- Suppression protegee (emprunts associes bloques)
- Detail consultable

### 3.3 UtilisateurController

- Liste avec recherche par nom ou email
- Creation avec validation d'email unique
- Modification avec verification d'unicite email
- Suppression protegee (emprunts associes bloques)
- Detail consultable

### 3.4 EmpruntController

- Liste des emprunts actifs et historique
- Creation d'emprunt (verification de disponibilite)
- Enregistrement de retour (mise a jour automatique de la disponibilite du livre)
- Detail consultable

## 4. Templates crees

Structure sous `src/main/resources/templates/` :

```
templates/
  index.html                    # Page d'accueil
  livres/
    liste.html                  # Liste des livres
    formulaire.html             # Creation / modification
    detail.html                 # Detail d'un livre
  utilisateurs/
    liste.html                  # Liste des utilisateurs
    formulaire.html             # Creation / modification
    detail.html                 # Detail d'un utilisateur
  emprunts/
    liste.html                  # Emprunts actifs + historique
    detail.html                 # Detail d'un emprunt
```

## 5. Nettoyage effectue

- **Suppression de `FlashMessageUtil.java`** : Remplacé par l'utilisation native de `RedirectAttributes` de Spring MVC
- **Suppression du dossier `servlet/`** : Etait vide, les Servlets historiques ont été supprimées lors des phases précédentes
- **Aucune reference a `HttpSession`** dans les nouveaux controllers

## 6. Points reportes

Aucun point n'est reporte a cette etape.

## 7. Validation

- [x] Compilation Maven reussie
- [x] Routes definies et coherentres
- [x] Controllers en place pour tous les modules
- [x] Templates de base crees
- [x] Gestion des erreurs et redirections implementees
- [x] Messages flash整合整合
- [x] Ancien mecanisme `HttpSession` supprime
- [x] Fichiers inutiles nettoyes

## 8. Critere de sortie

- [x] Les routes sont lisibles et coherentes
- [x] La couche web est modernisee
- [x] Les parcours sont mieux supportes par la structure HTTP
