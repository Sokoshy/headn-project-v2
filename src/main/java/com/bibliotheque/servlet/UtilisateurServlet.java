package com.bibliotheque.servlet;

import com.bibliotheque.config.CSRFUtil;
import com.bibliotheque.config.FlashMessageUtil;
import com.bibliotheque.service.UtilisateurService;
import com.bibliotheque.service.ServiceResult;
import com.bibliotheque.model.Utilisateur;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/utilisateurs")
public class UtilisateurServlet extends HttpServlet {
    private UtilisateurService utilisateurService;

    @Override
    public void init() throws ServletException {
        utilisateurService = new UtilisateurService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";
        FlashMessageUtil.consume(request);
        switch (action) {
            case "list":
                listerUtilisateurs(request, response);
                break;
            case "edit":
                afficherFormulaireEdit(request, response);
                break;
            default:
                listerUtilisateurs(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CSRFUtil.validateToken(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token CSRF invalide");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "add";

        switch (action) {
            case "add":
                ajouterUtilisateur(request, response);
                break;
            case "update":
                modifierUtilisateur(request, response);
                break;
            case "delete":
                supprimerUtilisateur(request, response);
                break;
            default:
                ajouterUtilisateur(request, response);
        }
    }

    private void listerUtilisateurs(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServiceResult<List<Utilisateur>> result = utilisateurService.getAllUtilisateurs();
        if (result.isSuccess()) {
            request.setAttribute("utilisateurs", result.getData());
        } else {
            request.setAttribute("error", result.getMessage());
        }
        CSRFUtil.generateToken(request);
        request.getRequestDispatcher("/WEB-INF/views/utilisateurs.jsp").forward(request, response);
    }

    private void ajouterUtilisateur(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String nom = request.getParameter("nom");
        String email = request.getParameter("email");

        ServiceResult<Utilisateur> result = utilisateurService.ajouterUtilisateur(nom, email);
        if (result.isSuccess()) {
            FlashMessageUtil.success(request, normalizeMessage(result.getMessage()));
        } else {
            FlashMessageUtil.error(request, normalizeMessage(result.getMessage()));
        }
        response.sendRedirect(request.getContextPath() + "/utilisateurs");
    }

    private void afficherFormulaireEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null) {
            ServiceResult<Utilisateur> result = utilisateurService.getUtilisateurById(idStr);
            if (result.isSuccess()) {
                request.setAttribute("utilisateur", result.getData());
                CSRFUtil.generateToken(request);
                request.getRequestDispatcher("/WEB-INF/views/utilisateurs.jsp").forward(request, response);
                return;
            }
        }
        response.sendRedirect(request.getContextPath() + "/utilisateurs");
    }

    private void modifierUtilisateur(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String nom = request.getParameter("nom");
        String email = request.getParameter("email");

        ServiceResult<Utilisateur> result = utilisateurService.modifierUtilisateur(idStr, nom, email);
        if (result.isSuccess()) {
            FlashMessageUtil.success(request, normalizeMessage(result.getMessage()));
        } else {
            FlashMessageUtil.error(request, normalizeMessage(result.getMessage()));
        }
        response.sendRedirect(request.getContextPath() + "/utilisateurs");
    }

    private void supprimerUtilisateur(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");

        ServiceResult<Boolean> result = utilisateurService.supprimerUtilisateur(idStr);
        if (result.isSuccess()) {
            FlashMessageUtil.success(request, normalizeMessage(result.getMessage()));
        } else {
            FlashMessageUtil.error(request, normalizeMessage(result.getMessage()));
        }
        response.sendRedirect(request.getContextPath() + "/utilisateurs");
    }

    private String normalizeMessage(String message) {
        if (message == null) {
            return null;
        }

        if (message.endsWith("!")) {
            return message.substring(0, message.length() - 1) + ".";
        }

        return message;
    }
}
