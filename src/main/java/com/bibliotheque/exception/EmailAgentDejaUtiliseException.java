package com.bibliotheque.exception;

public class EmailAgentDejaUtiliseException extends BusinessException {

    public EmailAgentDejaUtiliseException(String email) {
        super(String.format("L'adresse email '%s' est déjà utilisée par un autre agent", email), "EMAIL_AGENT_DEJA_UTILISE");
    }
}