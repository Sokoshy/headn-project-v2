package com.bibliotheque.exception;

public class LivreNotFoundException extends ResourceNotFoundException {

    public LivreNotFoundException(Long id) {
        super("Livre", id);
    }
}
