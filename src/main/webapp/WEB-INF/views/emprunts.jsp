<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestion des Emprunts - Gestionnaire de Bibliothèque</title>
    <link rel="stylesheet" type="text/css" href="css/styles.css">
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>📋 Gestion des Emprunts</h1>
            <p>Suivez et gérez les emprunts de livres de votre bibliothèque</p>
            <a href="${pageContext.request.contextPath}/" class="nav-link">← Retour à l'accueil</a>
        </div>

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

        <!-- Section NOUVEL EMPRUNT -->
        <div class="section section-add">
            <div class="section-header">
                <h2 class="section-title">
                    <span class="section-icon">📚</span>
                    NOUVEL EMPRUNT
                </h2>
                <p class="section-description">Enregistrez un nouvel emprunt de livre</p>
            </div>
            <div class="card card-add">
                <form action="${pageContext.request.contextPath}/emprunts" method="post" novalidate>
                    <input type="hidden" name="action" value="add"/>
                    <%= com.bibliotheque.config.CSRFUtil.getHiddenField(request) %>
                    <div class="form-grid">
                        <div class="form-field">
                            <label for="utilisateurId">👤 Utilisateur *</label>
                            <select name="utilisateurId" id="utilisateurId" class="form-control" required aria-required="true">
                                <option value="">-- Sélectionner un utilisateur --</option>
                                <c:forEach var="u" items="${utilisateurs}">
                                    <option value="${u.id}"><c:out value="${u.nom}"/> (<c:out value="${u.email}"/>)</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="form-field">
                            <label for="livreId">📖 Livre *</label>
                            <select name="livreId" id="livreId" class="form-control" required aria-required="true">
                                <option value="">-- Sélectionner un livre --</option>
                                <c:forEach var="l" items="${livresDisponibles}">
                                    <option value="${l.id}"><c:out value="${l.titre}"/> (<c:out value="${l.auteur}"/>)</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    <div class="form-actions">
                        <button type="submit" class="btn btn-success btn-large">📚 Enregistrer l'emprunt</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- Section EMPRUNTS ACTIFS -->
        <div class="section section-search">
            <div class="section-header">
                <h2 class="section-title">
                    <span class="section-icon">📖</span>
                    EMPRUNTS EN COURS
                </h2>
                <p class="section-description">Livres actuellement empruntés par les membres</p>
            </div>
            <div class="card card-search">
                <div class="table-container">
                    <table role="table" aria-label="Emprunts en cours">
                        <thead>
                            <tr>
                                <th scope="col">ID</th>
                                <th scope="col">Membre</th>
                                <th scope="col">Livre</th>
                                <th scope="col">Date emprunt</th>
                                <th scope="col">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="emprunt" items="${emprunts}">
                                <tr>
                                    <td><strong>#<c:out value="${emprunt.id}"/></strong></td>
                                    <td>
                                        <c:out value="${emprunt.nomUtilisateur}"/><br/>
                                        <small><c:out value="${emprunt.emailUtilisateur}"/></small>
                                    </td>
                                    <td>
                                        <c:out value="${emprunt.titreLivre}"/><br/>
                                        <small><c:out value="${emprunt.auteurLivre}"/></small>
                                    </td>
                                    <td><c:out value="${emprunt.dateEmprunt}"/></td>
                                    <td>
                                        <form method="post" action="${pageContext.request.contextPath}/emprunts" style="display:inline;">
                                            <input type="hidden" name="action" value="retour"/>
                                            <input type="hidden" name="id" value="<c:out value="${emprunt.id}"/>"/>
                                            <%= com.bibliotheque.config.CSRFUtil.getHiddenField(request) %>
                                            <button type="submit" class="btn btn-success btn-small">
                                                ✅ Enregistrer le retour
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty emprunts}">
                                <tr>
                                    <td colspan="5">
                                        <div class="empty-state">
                                            <span class="icon">📭</span>
                                            <h3>Aucun emprunt en cours</h3>
                                            <p>Tous les livres sont disponibles</p>
                                        </div>
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Section HISTORIQUE -->
        <div class="section section-collection">
            <div class="section-header">
                <h2 class="section-title">
                    <span class="section-icon">📋</span>
                    HISTORIQUE DES EMPRUNTS
                </h2>
                <p class="section-description">Historique complet des emprunts et retours</p>
            </div>
            <div class="card card-collection" id="historique">
                <div class="table-container">
                    <table role="table" aria-label="Historique des emprunts">
                        <thead>
                            <tr>
                                <th scope="col">ID</th>
                                <th scope="col">Membre</th>
                                <th scope="col">Livre</th>
                                <th scope="col">Date emprunt</th>
                                <th scope="col">Date retour</th>
                                <th scope="col">Statut</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="emprunt" items="${historique}">
                                <tr>
                                    <td><strong>#<c:out value="${emprunt.id}"/></strong></td>
                                    <td><c:out value="${emprunt.nomUtilisateur}"/></td>
                                    <td><c:out value="${emprunt.titreLivre}"/></td>
                                    <td><c:out value="${emprunt.dateEmprunt}"/></td>
                                    <td><c:out value="${emprunt.dateRetour}"/></td>
                                    <td>
                                        <span class="status-badge status-available">Terminé</span>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty historique}">
                                <tr>
                                    <td colspan="6">
                                        <div class="empty-state">
                                            <span class="icon">📭</span>
                                            <h3>Aucun historique</h3>
                                            <p>Les emprunts terminés apparaîtront ici</p>
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
            <p>&copy; 2025 Gestionnaire de Bibliothèque - Module Gestion des Emprunts</p>
        </div>
    </div>
</body>
</html>
