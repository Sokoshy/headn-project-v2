package com.bibliotheque.model.audit;

import com.bibliotheque.model.AuditAction;

import java.time.LocalDateTime;

/**
 * Projection used to display audit entries. Built by the repository via a constructor expression.
 */
public record AuditEntry(
        Long id,
        Long loanId,
        String loanBookTitle,
        String loanUserName,
        Long agentId,
        String agentName,
        String agentEmail,
        AuditAction action,
        LocalDateTime dateAction
) {
}