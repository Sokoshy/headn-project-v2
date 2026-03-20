package com.bibliotheque.exception;

public class EmpruntNotFoundException extends ResourceNotFoundException {

    public EmpruntNotFoundException(Long id) {
        super("Emprunt", id);
    }
}
