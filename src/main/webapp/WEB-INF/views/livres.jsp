<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestion des Livres - Gestionnaire de Bibliothèque</title>
    <meta name="description" content="Gérez votre collection de livres facilement">
    <link rel="stylesheet" type="text/css" href="css/styles.css">
    <script>
        // Variable pour suivre l'état d'affichage
        let showingDisponibles = ${param.action == 'disponibles' ? 'true' : 'false'};

        // Fonction pour basculer entre "disponibles uniquement" et "tous les livres"
        function toggleDisponibles() {
            showingDisponibles = !showingDisponibles;
            const button = document.getElementById('toggle-disponibles');

            if (showingDisponibles) {
                // Afficher seulement les disponibles
                button.innerHTML = '📚 Afficher tous les livres';
                button.classList.remove('btn-info');
                button.classList.add('btn-warning');
                window.location.href = '${pageContext.request.contextPath}/livres?action=disponibles#collection';
            } else {
                // Afficher tous les livres
                button.innerHTML = '✅ Afficher seulement les livres disponibles';
                button.classList.remove('btn-warning');
                button.classList.add('btn-info');
                window.location.href = '${pageContext.request.contextPath}/livres#collection';
            }
        }

        document.addEventListener('DOMContentLoaded', function() {
            // Animation pour les livres disponibles
            const urlParams = new URLSearchParams(window.location.search);
            if (urlParams.get('action') === 'disponibles') {
                setTimeout(function() {
                    const tableContainer = document.querySelector('#collection .table-container');
                    if (tableContainer) {
                        tableContainer.classList.add('highlight-table');
                        setTimeout(function() {
                            tableContainer.classList.remove('highlight-table');
                        }, 2000);
                    }
                }, 500);
            }

            // Scroll vers la section d'édition si applicable
            <c:if test="${not empty livre}">
            setTimeout(function() {
                const editSection = document.querySelector('.section-edit');
                if (editSection) {
                    editSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
                    editSection.classList.add('highlight-section');
                    setTimeout(function() {
                        editSection.classList.remove('highlight-section');
                    }, 3000);
                }
            }, 500);
            </c:if>
        });
    </script>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>📖 Gestion des Livres</h1>
            <p>Gérez votre collection de livres facilement</p>
            <a href="${pageContext.request.contextPath}/" class="nav-link">← Retour à l'accueil</a>
        </div>

        <!-- Messages de notification -->
        <c:if test="${not empty message}">
            <div class="alert alert-success" role="alert" aria-live="polite">
                ✅ <c:out value="${message}"/>
            </div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alert alert-error" role="alert" aria-live="assertive">
                ❌ <c:out value="${error}"/>
            </div>
        </c:if>

        <!-- Section AJOUT DE LIVRE -->
        <div class="section section-add">
            <div class="section-header">
                <h2 class="section-title">
                    <span class="section-icon">📝</span>
                    AJOUTER UN LIVRE
                </h2>
                <p class="section-description">Ajoutez un nouveau livre à votre collection</p>
            </div>
            <div class="card card-add">
                <form action="${pageContext.request.contextPath}/livres" method="post" novalidate>
                    <input type="hidden" name="action" value="add"/>
                    <%= com.bibliotheque.config.CSRFUtil.getHiddenField(request) %>
                    <div class="form-grid">
                        <div class="form-field">
                            <label for="titre-livre">📖 Titre du livre *</label>
                            <input type="text" id="titre-livre" name="titre" class="form-control"
                                   placeholder="Ex: Le Petit Prince" required aria-required="true"/>
                        </div>
                        <div class="form-field">
                            <label for="auteur-livre">👤 Auteur *</label>
                            <input type="text" id="auteur-livre" name="auteur" class="form-control"
                                   placeholder="Ex: K&R, J.K. Rowling, Antoine de Saint-Exupéry" required aria-required="true"/>
                        </div>
                    </div>
                    <div class="form-actions">
                        <button type="submit" class="btn btn-success btn-large">
                            ➕ Ajouter à la collection
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <!-- Section RECHERCHE -->
        <div class="section section-search">
            <div class="section-header">
                <h2 class="section-title">
                    <span class="section-icon">🔍</span>
                    RECHERCHER DES LIVRES
                </h2>
                <p class="section-description">Trouvez rapidement un livre dans votre collection</p>
            </div>
            <div class="card card-search">
                <form method="get" action="${pageContext.request.contextPath}/livres">
                    <input type="hidden" name="action" value="recherche"/>
                    <div class="search-container">
                        <div class="search-input-group">
                            <label for="terme-recherche">Rechercher par titre ou auteur</label>
                            <input type="search" id="terme-recherche" name="terme" class="form-control search-input"
                                   placeholder="Tapez votre recherche..."
                                   value="<c:out value="${terme != null ? terme : ''}"/>"/>
                        </div>
                        <div class="search-buttons">
                            <button type="submit" class="btn btn-primary">🔍 Rechercher</button>
                            <a href="${pageContext.request.contextPath}/livres" class="btn btn-secondary">🔄 Tout afficher</a>
                        </div>
                    </div>
                </form>
                <div class="filter-actions">
                    <c:choose>
                        <c:when test="${param.action == 'disponibles'}">
                            <button id="toggle-disponibles" class="btn btn-info" onclick="toggleDisponibles()">
                                📚 Afficher tous les livres
                            </button>
                        </c:when>
                        <c:otherwise>
                            <button id="toggle-disponibles" class="btn btn-info" onclick="toggleDisponibles()">
                                ✅ Afficher seulement les livres disponibles
                            </button>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <!-- Section EDITION (conditionnelle) -->
        <c:if test="${not empty livre}">
            <div class="section section-edit">
                <div class="section-header">
                    <h2 class="section-title">
                        <span class="section-icon">✏️</span>
                        MODIFIER LE LIVRE
                    </h2>
                    <p class="section-description">Modifiez les informations du livre sélectionné</p>
                </div>
                <div class="card card-edit">
                    <form action="${pageContext.request.contextPath}/livres" method="post" novalidate>
                        <input type="hidden" name="action" value="update"/>
                        <input type="hidden" name="id" value="<c:out value="${livre.id}"/>"/>
                        <input type="hidden" name="disponible" value="<c:out value="${livre.disponible}"/>"/>
                        <%= com.bibliotheque.config.CSRFUtil.getHiddenField(request) %>
                        <div class="form-grid">
                            <div class="form-field">
                                <label for="edit-titre-livre">📖 Titre du livre *</label>
                                <input type="text" id="edit-titre-livre" name="titre" class="form-control"
                                       value="<c:out value="${livre.titre}"/>" required aria-required="true"/>
                            </div>
                            <div class="form-field">
                                <label for="edit-auteur-livre">👤 Auteur *</label>
                                <input type="text" id="edit-auteur-livre" name="auteur" class="form-control"
                                       value="<c:out value="${livre.auteur}"/>" required aria-required="true"/>
                            </div>
                        </div>
                        <p class="section-description">
                            Statut actuel :
                            <c:choose>
                                <c:when test="${livre.disponible}">
                                    <span class="status-badge status-available">Disponible</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="status-badge status-unavailable">Emprunte</span>
                                </c:otherwise>
                            </c:choose>
                        </p>
                        <div class="form-actions">
                            <button type="submit" class="btn btn-warning">💾 Enregistrer les modifications</button>
                            <a href="${pageContext.request.contextPath}/livres" class="btn btn-secondary">❌ Annuler</a>
                        </div>
                    </form>
                </div>
            </div>
        </c:if>

        <!-- Section COLLECTION -->
        <div class="section section-collection">
            <div class="section-header">
                <h2 class="section-title">
                    <span class="section-icon">📚</span>
                    <c:choose>
                        <c:when test="${param.action == 'disponibles'}">
                            LIVRES DISPONIBLES SEULEMENT
                            <span class="filter-indicator active">🔍 Filtrage actif</span>
                        </c:when>
                        <c:otherwise>
                            VOTRE COLLECTION
                        </c:otherwise>
                    </c:choose>
                </h2>
                <p class="section-description">
                    <c:choose>
                        <c:when test="${param.action == 'disponibles'}">
                            Liste des livres actuellement disponibles à l'emprunt
                        </c:when>
                        <c:otherwise>
                            Consultez et gérez tous les livres de votre bibliothèque
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>
            <div class="card card-collection" id="collection">
                <div class="table-container">
                    <table role="table" aria-label="Liste des livres">
                        <thead>
                            <tr>
                                <th scope="col">ID</th>
                                <th scope="col">Titre</th>
                                <th scope="col">Auteur</th>
                                <th scope="col">Disponibilité</th>
                                <th scope="col">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="livre" items="${livres}">
                                <tr>
                                    <td><strong>#<c:out value="${livre.id}"/></strong></td>
                                    <td><c:out value="${livre.titre}"/></td>
                                    <td><c:out value="${livre.auteur}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${livre.disponible}">
                                                <span class="status-badge status-available">Disponible</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-badge status-unavailable">Emprunté</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div class="actions">
                                            <a href="${pageContext.request.contextPath}/livres?action=edit&id=${livre.id}"
                                               class="btn btn-edit btn-small">Modifier</a>
                                            <form method="post" action="${pageContext.request.contextPath}/livres" style="display:inline;">
                                                <input type="hidden" name="action" value="delete"/>
                                                <input type="hidden" name="id" value="<c:out value="${livre.id}"/>"/>
                                                <%= com.bibliotheque.config.CSRFUtil.getHiddenField(request) %>
                                                <button type="submit" class="btn btn-delete btn-small"
                                                        onclick="return confirm('Êtes-vous sûr de vouloir supprimer ce livre ?');">Supprimer</button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty livres}">
                                <tr>
                                    <td colspan="5">
                                        <div class="empty-state">
                                            <span class="icon">📭</span>
                                            <h3>Aucun livre trouvé</h3>
                                            <p>
                                                <c:choose>
                                                    <c:when test="${param.action == 'disponibles'}">
                                                        Aucun livre disponible pour le moment
                                                    </c:when>
                                                    <c:otherwise>
                                                        Ajoutez votre premier livre ou modifiez vos critères de recherche
                                                    </c:otherwise>
                                                </c:choose>
                                            </p>
                                        </div>
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <div class="footer">
            <p>&copy; 2025 Gestionnaire de Bibliothèque - Module Gestion des Livres</p>
        </div>
    </div>
</body>
</html>
