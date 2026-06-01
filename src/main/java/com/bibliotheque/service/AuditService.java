package com.bibliotheque.service;

import com.bibliotheque.model.Agent;
import com.bibliotheque.model.AuditAction;
import com.bibliotheque.model.AuditLoan;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.audit.AuditEntry;
import com.bibliotheque.repository.AuditLoanRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuditService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final AuditLoanRepository auditLoanRepository;

    public AuditService(AuditLoanRepository auditLoanRepository) {
        this.auditLoanRepository = auditLoanRepository;
    }

    @Transactional
    public AuditLoan enregistrerCreation(Emprunt emprunt, Agent agent) {
        return auditLoanRepository.save(new AuditLoan(emprunt, agent, AuditAction.CREATION));
    }

    @Transactional
    public AuditLoan enregistrerRetour(Emprunt emprunt, Agent agent) {
        return auditLoanRepository.save(new AuditLoan(emprunt, agent, AuditAction.RETURN));
    }

    @Transactional
    public AuditLoan enregistrer(Emprunt emprunt, Agent agent, AuditAction action) {
        return auditLoanRepository.save(new AuditLoan(emprunt, agent, action));
    }

    public Page<AuditEntry> lister(Long agentId, AuditAction action, LocalDate from, LocalDate to, int page) {
        PageRequest pageRequest = PageRequest.of(page, DEFAULT_PAGE_SIZE);
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.atTime(LocalTime.MAX);

        Specification<AuditLoan> spec = buildSpecification(agentId, action, fromDateTime, toDateTime);
        return auditLoanRepository.findAll(spec, pageRequest).map(this::toEntry);
    }

    public List<AuditEntry> historiquePourEmprunt(Long empruntId) {
        return auditLoanRepository.findByLoanIdOrderByDateActionAsc(empruntId)
                .stream()
                .map(this::toEntry)
                .toList();
    }

    /**
     * Builds a Specification that conditionally adds each non-null filter as a
     * predicate. Replaces the previous JPQL with {@code (:param IS NULL OR ...)}
     * which PostgreSQL 17 rejected ("could not determine data type of parameter")
     * when all parameters were null.
     */
    private Specification<AuditLoan> buildSpecification(Long agentId, AuditAction action,
                                                        LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (agentId != null) {
                predicates.add(cb.equal(root.get("agent").get("id"), agentId));
            }
            if (action != null) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (fromDateTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateAction"), fromDateTime));
            }
            if (toDateTime != null) {
                // Strict less-than matches the previous JPQL (:to IS NULL OR a.dateAction < :to).
                // Combined with toDateTime = to.atTime(LocalTime.MAX), this includes the
                // full target day up to the last nanosecond.
                predicates.add(cb.lessThan(root.get("dateAction"), toDateTime));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private AuditEntry toEntry(AuditLoan entry) {
        return new AuditEntry(
                entry.getId(),
                entry.getLoan() != null ? entry.getLoan().getId() : null,
                entry.getLoan() != null && entry.getLoan().getLivre() != null
                        ? entry.getLoan().getLivre().getTitre() : null,
                entry.getLoan() != null && entry.getLoan().getUtilisateur() != null
                        ? entry.getLoan().getUtilisateur().getNom() : null,
                entry.getAgent() != null ? entry.getAgent().getId() : null,
                entry.getAgent() != null ? entry.getAgent().getNom() : null,
                entry.getAgent() != null ? entry.getAgent().getEmail() : null,
                entry.getAction(),
                entry.getDateAction()
        );
    }
}
