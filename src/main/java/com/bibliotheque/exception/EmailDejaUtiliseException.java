package com.bibliotheque.exception;

public class EmailDejaUtiliseException extends BusinessException {

    public EmailDejaUtiliseException(String email) {
        super(String.format("L'adresse email '%s' est déjà utilisée", email), "EMAIL_DEJA_UTILISE");
    }
}
