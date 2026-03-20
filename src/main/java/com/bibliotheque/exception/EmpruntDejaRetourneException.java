package com.bibliotheque.exception;

public class EmpruntDejaRetourneException extends BusinessException {

    public EmpruntDejaRetourneException(Long empruntId) {
        super(String.format("L'emprunt %d a déjà été retourné", empruntId), "EMPRUNT_DEJA_RETOURNE");
    }
}
