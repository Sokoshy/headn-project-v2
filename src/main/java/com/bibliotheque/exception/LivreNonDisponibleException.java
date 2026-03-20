package com.bibliotheque.exception;

public class LivreNonDisponibleException extends BusinessException {

    public LivreNonDisponibleException(String titre) {
        super(String.format("Le livre '%s' n'est pas disponible pour l'emprunt", titre), "LIVRE_NON_DISPONIBLE");
    }

    public LivreNonDisponibleException(Long livreId) {
        super(String.format("Le livre avec l'identifiant %d n'est pas disponible pour l'emprunt", livreId), "LIVRE_NON_DISPONIBLE");
    }
}
