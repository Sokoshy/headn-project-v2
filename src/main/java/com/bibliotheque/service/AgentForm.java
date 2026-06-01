package com.bibliotheque.service;

import com.bibliotheque.model.Role;

/**
 * Form data for creating/updating an agent. Plain password is provided by the
 * controller layer and hashed by AgentService before persistence.
 */
public record AgentForm(
        String nom,
        String email,
        String motDePasse,
        String motDePasseConfirmation,
        Role role,
        String telephone
) {
}