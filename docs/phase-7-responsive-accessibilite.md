# Phase 7 - Responsive et Accessibilite

## 1. Contexte

Cette phase marque l'amelioration de l'interface pour garantir une experience utilisateur de qualite sur tous les formats d'ecrans, ainsi qu'une meilleure accessibilite pour les utilisateurs en situation de handicap.

## 2. Decisions prises

### 2.1 Approche Mobile-First

Adoption d'une approche **mobile-first** avec des breakpoints progressifs :

```
Base (mobile) : < 576px
sm (tablet)   : >= 576px
md (desktop)  : >= 768px
lg (large)    : >= 992px
xl (xlarge)   : >= 1200px
```

Cette approche permet de :
- Prioriser l'experience mobile
- Ajouter progressivement des ameliorations pour les ecrans plus grands
- Reduire la quantite de CSS conditionnel

### 2.2 Breakpoints CSS

Les breakpoints sont definis via des variables CSS et des media queries min-width :

- Variables CSS pour les espacements et hauteurs adaptables
- Media queries pour les adaptations specifiques de navigation, tableaux et formulaires

### 2.3 Ameliorations Navigation

Navigation adaptee au mobile :
- Sur mobile : navigation compacte avec icones et labels empiles
- Sur tablette : icones + labels sur une seule ligne
- Sur desktop : espacement plus confortable

### 2.4 Adaptation des Tableaux

Conservation de la **semantique tabulaire** sur mobile :
- Les tableaux restent de vrais tableaux
- Le conteneur garde un scroll horizontal sur petit ecran
- Les cellules et actions sont compactées pour conserver la lisibilite
- Des `caption` invisibles sont ajoutees pour exposer le contexte aux lecteurs d'ecran

Cette approche evite de masquer les en-tetes ou de reconstruire les libelles via pseudo-elements CSS.

### 2.5 Adaptation des Formulaires

- Inputs avec hauteur minimale de 44px (meilleure cible tactile)
- Grille de formulaire passant en colonne unique sur mobile
- Labels toujours visibles et bien contrastes
- Champs de recherche avec labels accessibles masques visuellement

### 2.6 Accessibilite - Focus Visible

Ajout de styles de focus explicites :
- `:focus-visible` avec outline de 2px en couleur primaire
- Focus visible uniquement pour la navigation clavier
- Pas de focus automatique pour les clics souris

### 2.7 Accessibilite - Skip Link

Ajout d'un lien "Aller au contenu principal" :
- Cache visuellement mais accessible au clavier
- Apparait au premier focus TAB
- Permet de sauter la navigation

### 2.8 Accessibilite - Structure Semantique

Amelioration de la structure HTML :
- Un seul `main` global dans le layout
- Fragments de pages injectes sans `main` imbrique
- `role="navigation"` sur la navigation principale
- `role="contentinfo"` sur le pied de page
- `aria-label` sur les elements de navigation
- `aria-current="page"` sur l'item de navigation actif
- `aria-live` sur les messages flash (polite/assertive)
- `scope="col"` sur les en-tetes de tableaux
- `aria-hidden="true"` sur les icones SVG decoratives
- `id="main-content"` sur le contenu principal

### 2.9 Accessibilite - Contrastes

Verification des contrastes :
- Texte principal : #334155 sur #ffffff (ratio > 7:1)
- Texte secondaire : #64748b sur #ffffff (ratio > 4.5:1)
- Texte sur primaire : #ffffff sur #4f46e5 (ratio > 4.5:1)
- Badges : couleurs avec contraste suffisant

### 2.10 Accessibilite - Reduced Motion

Ajout d'une media query `prefers-reduced-motion` :
- Desactive les animations et transitions
- Desactive le smooth scrolling

### 2.11 Accessibilite - Messages d'erreur

Amelioration des messages d'erreur :
- Ajout d'une icone visuelle (warning)
- Texte en gras pour meilleure lisibilite
- Association correcte label/input via `for`/`id`
- Suppression des declarations CSS ARIA invalides

## 3. Templates modifies

### 3.1 Layout global (`layout/main.html`)

Ajouts et corrections :
- Skip link pour l'accessibilite
- `aria-label` sur la navigation
- `aria-current` sur l'item actif
- `aria-live` sur les messages flash
- `role="contentinfo"` sur le footer
- `id="main-content"` sur le `main`
- Main unique conserve au niveau du layout

### 3.2 Page Livres (`livres/liste.html`)

Modifications :
- Fragment injecte sans `main` imbrique
- Label accessible ajoute au champ de recherche
- `caption` ajoutee au tableau
- SVG decoratifs masques avec `aria-hidden="true"`

### 3.3 Page Utilisateurs (`utilisateurs/liste.html`)

Modifications :
- Fragment injecte sans `main` imbrique
- Label accessible ajoute au champ de recherche
- `caption` ajoutee au tableau
- SVG decoratifs masques avec `aria-hidden="true"`

### 3.4 Page Emprunts (`emprunts/liste.html`)

Modifications :
- Fragment injecte sans `main` imbrique
- `caption` ajoutee aux tableaux actifs et historique
- SVG decoratifs masques avec `aria-hidden="true"`
- Actions conservees dans la structure tabulaire sur mobile

### 3.5 Pages detail et formulaires

Modifications :
- Suppression des `main` imbriques dans les pages accueil, detail et formulaires
- Ajout de `aria-hidden="true"` sur les icones decoratives restantes, y compris les fleches de retour

## 4. CSS ajoute/modifie

### 4.1 Nouvelles variables CSS

```css
--z-modal: 400;
--z-skip-link: 500;
--container-padding: var(--spacing-4);
--header-height: auto;
```

### 4.2 Nouvelles classes CSS

- `.skip-link` : lien de saut pour l'accessibilite
- `.sr-only` : classe pour masquer visuellement mais garder accessible
- Selecteurs `:focus-visible` pour le focus clavier
- Media queries mobile-first pour les adaptations responsive

### 4.3 Adaptations responsive

- Navigation : disposition et taille adaptees
- Tableaux : scroll horizontal conserve sur mobile avec cellules compactees
- Formulaires : colonne unique sur mobile
- Actions bar : empilement vertical sur mobile

## 5. Points reportes

Aucun point n'est reporte a cette etape.

## 6. Validation

- [x] Compilation Maven reussie
- [x] Approche mobile-first en place
- [x] Breakpoints definis et documentes
- [x] Tableaux mobiles conservant leur semantique
- [x] Formulaires adaptes au mobile
- [x] Focus visible pour la navigation clavier
- [x] Contrastes verifies et suffisants
- [x] Navigation clavier fonctionnelle (skip link)
- [x] Labels correctement associes aux champs
- [x] Structure semantique HTML correcte
- [x] Messages d'erreur comprehensibles et visibles

## 7. Critere de sortie

- [x] L'interface reste lisible sur mobile
- [x] L'application est utilisable au clavier
- [x] Le niveau d'accessibilite est nettement ameliore
