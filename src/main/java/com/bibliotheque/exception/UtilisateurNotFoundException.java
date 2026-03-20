package com.bibliotheque.exception;

public class UtilisateurNotFoundException extends ResourceNotFoundException {

    public UtilisateurNotFoundException(Long id) {
        super("Utilisateur", id);
    }

    public UtilisateurNotFoundException(String email) {
        super(String.format("Utilisateur avec l'email '%s' non trouvé", email));
    }
}
