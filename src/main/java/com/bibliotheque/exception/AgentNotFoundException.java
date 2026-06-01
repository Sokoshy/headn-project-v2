package com.bibliotheque.exception;

public class AgentNotFoundException extends ResourceNotFoundException {

    public AgentNotFoundException(Long id) {
        super("Agent", id);
    }

    public AgentNotFoundException(String email) {
        super(String.format("Agent avec l'email '%s' non trouvé", email));
    }
}