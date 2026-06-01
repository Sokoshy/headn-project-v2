package com.bibliotheque.repository;

import com.bibliotheque.BibliothequeApplication;
import com.bibliotheque.model.Agent;
import com.bibliotheque.model.AuditAction;
import com.bibliotheque.model.AuditLoan;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Role;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.support.PostgresIntegrationTestBase;
import jakarta.persistence.criteria.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AuditLoanRepository} against a real PostgreSQL 17
 * container. Covers the regression where the previous {@code @Query} with
 * {@code (:param IS NULL OR ...)} triggered a "could not determine data type of
 * parameter" error when every filter was null.
 */
@SpringBootTest(classes = BibliothequeApplication.class)
class AuditLoanRepositoryPostgresTest extends PostgresIntegrationTestBase {

    @Autowired
    private AuditLoanRepository auditLoanRepository;

    @Autowired
    private EmpruntRepository empruntRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private AgentRepository agentRepository;

    private Agent agent;
    private Emprunt emprunt;

    @BeforeEach
    void setUp() {
        // Wipe in FK-dependency order: audit_loans -> emprunts -> users/books -> agents.
        // agents is last because audit_loans.agent_id has ON DELETE RESTRICT.
        auditLoanRepository.deleteAllInBatch();
        empruntRepository.deleteAllInBatch();
        utilisateurRepository.deleteAllInBatch();
        livreRepository.deleteAllInBatch();
        agentRepository.deleteAllInBatch();

        agent = agentRepository.saveAndFlush(
                new Agent("Alice", "alice-audit-repository@example.com", "HASH", Role.LIBRARIAN));
        Livre livre = livreRepository.saveAndFlush(new Livre("Dune", "Frank Herbert"));
        Utilisateur user = utilisateurRepository.saveAndFlush(
                new Utilisateur("Bob", "bob-audit-repository@example.com"));
        emprunt = new Emprunt(user, livre);
        emprunt.setDateRetourPrevue(LocalDate.now().plusDays(30));
        empruntRepository.saveAndFlush(emprunt);
    }

    @Test
    @DisplayName("findAll(spec, page) avec tous les filtres null ne lève plus d'erreur PG17")
    void lister_tousFiltresNull_retourneToutesLesLignes() {
        auditLoanRepository.saveAndFlush(new AuditLoan(emprunt, agent, AuditAction.CREATION));
        auditLoanRepository.saveAndFlush(new AuditLoan(emprunt, agent, AuditAction.RETURN));

        // Mirror what AuditService.lister does when all filters are null: an
        // empty AND predicate list. With the old JPQL query this triggered the
        // PG17 "could not determine data type of parameter" error.
        Specification<AuditLoan> spec = (root, query, cb) -> cb.and(new Predicate[0]);

        Page<AuditLoan> page = auditLoanRepository.findAll(spec, PageRequest.of(0, 20));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("findAll(spec, page) avec filtre action ne renvoie que les lignes correspondantes")
    void lister_avecFiltreAction_retourneSeulementLesLignesCorrespondantes() {
        auditLoanRepository.saveAndFlush(new AuditLoan(emprunt, agent, AuditAction.CREATION));
        auditLoanRepository.saveAndFlush(new AuditLoan(emprunt, agent, AuditAction.RETURN));

        Specification<AuditLoan> spec = (root, query, cb) ->
                cb.equal(root.get("action"), AuditAction.CREATION);

        Page<AuditLoan> page = auditLoanRepository.findAll(spec, PageRequest.of(0, 20));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getAction()).isEqualTo(AuditAction.CREATION);
    }

    @Test
    @DisplayName("findAll(spec, page) avec filtre agent ne renvoie que les lignes de cet agent")
    void lister_avecFiltreAgent_retourneSeulementLesLignesDeCetAgent() {
        Agent other = agentRepository.saveAndFlush(
                new Agent("Other", "other-audit-repository@example.com", "HASH", Role.LIBRARIAN));
        auditLoanRepository.saveAndFlush(new AuditLoan(emprunt, agent, AuditAction.CREATION));
        auditLoanRepository.saveAndFlush(new AuditLoan(emprunt, other, AuditAction.CREATION));

        Specification<AuditLoan> spec = (root, query, cb) ->
                cb.equal(root.get("agent").get("id"), agent.getId());

        Page<AuditLoan> page = auditLoanRepository.findAll(spec, PageRequest.of(0, 20));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getAgent().getId()).isEqualTo(agent.getId());
    }

    @Test
    @DisplayName("findAll(spec, page) avec filtre date range borne les résultats")
    void lister_avecFiltreDateRange_borneLesResultats() {
        AuditLoan old = new AuditLoan(emprunt, agent, AuditAction.CREATION);
        // Force a date in the past so it falls outside the [from, to[ window.
        old.setDateAction(LocalDateTime.now().minusYears(2));
        auditLoanRepository.saveAndFlush(old);
        auditLoanRepository.saveAndFlush(new AuditLoan(emprunt, agent, AuditAction.RETURN));

        LocalDateTime from = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime to = LocalDate.now().plusDays(1).atTime(java.time.LocalTime.MAX);

        Specification<AuditLoan> spec = (root, query, cb) -> cb.and(
                List.of(
                        cb.greaterThanOrEqualTo(root.get("dateAction"), from),
                        cb.lessThan(root.get("dateAction"), to)
                ).toArray(new Predicate[0])
        );

        Page<AuditLoan> page = auditLoanRepository.findAll(spec, PageRequest.of(0, 20));

        // The "old" entry is two years in the past and must be excluded.
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getAction()).isEqualTo(AuditAction.RETURN);
    }
}
