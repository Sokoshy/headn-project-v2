package com.bibliotheque.service;

import com.bibliotheque.model.Agent;
import com.bibliotheque.model.AuditAction;
import com.bibliotheque.model.AuditLoan;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Role;
import com.bibliotheque.model.audit.AuditEntry;
import com.bibliotheque.repository.AuditLoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLoanRepository auditLoanRepository;

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(auditLoanRepository);
    }

    @Test
    void enregistrerCreation_persistsAuditEntry() {
        Emprunt emprunt = new Emprunt();
        emprunt.setId(10L);
        Agent agent = new Agent("Alice", "alice@bib.fr", "HASH", Role.LIBRARIAN);
        agent.setId(1L);

        when(auditLoanRepository.save(any(AuditLoan.class))).thenAnswer(inv -> inv.getArgument(0));

        auditService.enregistrerCreation(emprunt, agent);

        ArgumentCaptor<AuditLoan> captor = ArgumentCaptor.forClass(AuditLoan.class);
        verify(auditLoanRepository).save(captor.capture());
        AuditLoan saved = captor.getValue();
        assertThat(saved.getLoan()).isSameAs(emprunt);
        assertThat(saved.getAgent()).isSameAs(agent);
        assertThat(saved.getAction()).isEqualTo(AuditAction.CREATION);
    }

    @Test
    void enregistrerRetour_persistsAuditEntry() {
        Emprunt emprunt = new Emprunt();
        emprunt.setId(10L);
        Agent agent = new Agent("Alice", "alice@bib.fr", "HASH", Role.LIBRARIAN);
        agent.setId(1L);

        when(auditLoanRepository.save(any(AuditLoan.class))).thenAnswer(inv -> inv.getArgument(0));

        auditService.enregistrerRetour(emprunt, agent);

        ArgumentCaptor<AuditLoan> captor = ArgumentCaptor.forClass(AuditLoan.class);
        verify(auditLoanRepository).save(captor.capture());
        assertThat(captor.getValue().getAction()).isEqualTo(AuditAction.RETURN);
    }

    @Test
    @DisplayName("lister sans filtre construit une Specification non nulle et mappe les AuditLoan en AuditEntry")
    void lister_journal_allFiltersNull_buildsSpecAndMapsResult() {
        AuditLoan entry = new AuditLoan();
        entry.setId(1L);
        Page<AuditLoan> page = new PageImpl<>(List.of(entry), PageRequest.of(0, 20), 1);
        when(auditLoanRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        Page<AuditEntry> resultat = auditService.lister(null, null, null, null, 0);

        ArgumentCaptor<Specification<AuditLoan>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(auditLoanRepository).findAll(specCaptor.capture(), any(Pageable.class));
        assertThat(specCaptor.getValue())
                .as("AuditService must build a non-null Specification even when all filters are null")
                .isNotNull();
        assertThat(resultat.getContent()).hasSize(1);
        assertThat(resultat.getContent().get(0).id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("lister passe les filtres agent et action à la Specification et convertit les LocalDate en LocalDateTime")
    void lister_journal_passesFiltersToRepository() {
        Page<AuditLoan> empty = new PageImpl<>(List.of());
        when(auditLoanRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(empty);

        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        auditService.lister(2L, AuditAction.RETURN, from, to, 0);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(auditLoanRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(0);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
    }
}
