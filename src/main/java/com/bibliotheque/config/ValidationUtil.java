package com.bibliotheque.config;

import java.util.regex.Pattern;

public class ValidationUtil {
    
    // Patterns de validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern NOM_PATTERN = Pattern.compile(
        "^[a-zA-ZÀ-ÿ\\s\\-']{2,50}$"
    );
    
    private static final Pattern TITRE_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9À-ÿ\\s\\-\\'\":,!?.;()\\[\\]{}]{1,200}$"
    );
    
    private static final Pattern AUTEUR_PATTERN = Pattern.compile(
        "^[a-zA-ZÀ-ÿ\\s\\-'.&]{1,100}$"
    );
    
    /**
     * Valide une adresse email
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Valide un nom (utilisateur, auteur)
     */
    public static boolean isValidNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            return false;
        }
        return NOM_PATTERN.matcher(nom.trim()).matches();
    }
    
    /**
     * Valide un titre de livre
     */
    public static boolean isValidTitre(String titre) {
        if (titre == null || titre.trim().isEmpty()) {
            return false;
        }
        return TITRE_PATTERN.matcher(titre.trim()).matches();
    }
    
    /**
     * Valide un nom d'auteur
     */
    public static boolean isValidAuteur(String auteur) {
        if (auteur == null || auteur.trim().isEmpty()) {
            return false;
        }
        return AUTEUR_PATTERN.matcher(auteur.trim()).matches();
    }
    
    /**
     * Valide un identifiant numérique
     */
    public static boolean isValidId(String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) {
            return false;
        }
        try {
            int id = Integer.parseInt(idStr.trim());
            return id > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Nettoie et échappe les caractères HTML dangereux
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input.trim()
                   .replaceAll("&", "&amp;")
                   .replaceAll("<", "&lt;")
                   .replaceAll(">", "&gt;")
                   .replaceAll("\"", "&quot;")
                   .replaceAll("'", "&#39;");
    }
    
    /**
     * Limite la longueur d'une chaîne
     */
    public static String limitLength(String input, int maxLength) {
        if (input == null) {
            return "";
        }
        if (input.length() <= maxLength) {
            return input;
        }
        return input.substring(0, maxLength);
    }
    
    /**
     * Valide qu'une chaîne n'est pas vide après nettoyage
     */
    public static boolean isNotEmpty(String input) {
        return input != null && !input.trim().isEmpty();
    }
}