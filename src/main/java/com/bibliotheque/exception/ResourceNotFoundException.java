package com.bibliotheque.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s avec l'identifiant %d non trouvé", resourceName, id), "RESOURCE_NOT_FOUND");
    }
}
