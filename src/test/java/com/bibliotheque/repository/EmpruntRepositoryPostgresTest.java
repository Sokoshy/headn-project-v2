package com.bibliotheque.repository;

import com.bibliotheque.BibliothequeApplication;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.support.PostgresIntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = BibliothequeApplication.class)
class EmpruntRepositoryPostgresTest extends PostgresIntegrationTestBase {

    @Autowired
    private EmpruntRepository empruntRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @BeforeEach
    void cleanDatabase() {
        empruntRepository.deleteAllInBatch();
        utilisateurRepository.deleteAllInBatch();
        livreRepository.deleteAllInBatch();
    }

    @Test
    void dateRetourPrevue_isRequiredByDatabase() {
        Livre livre = livreRepository.save(new Livre("Dune", "Frank Herbert"));
        Utilisateur alice = utilisateurRepository.save(new Utilisateur("Alice", "alice-default@example.com"));

        Emprunt emprunt = new Emprunt(alice, livre);
        // Without dateRetourPrevue, the NOT NULL constraint rejects the insert
        assertThatThrownBy(() -> empruntRepository.saveAndFlush(emprunt))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    void dateRetourPrevue_canBeSetAndRead() {
        Livre livre = livreRepository.save(new Livre("Foundation", "Isaac Asimov"));
        Utilisateur alice = utilisateurRepository.save(new Utilisateur("Alice", "alice-expected@example.com"));

        Emprunt emprunt = new Emprunt(alice, livre);
        LocalDate expected = LocalDate.now().plusDays(30);
        emprunt.setDateRetourPrevue(expected);
        Emprunt saved = empruntRepository.saveAndFlush(emprunt);

        Emprunt loaded = empruntRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getDateRetourPrevue()).isEqualTo(expected);
    }

    private Emprunt createEmprunt(Utilisateur u, Livre l) {
        Emprunt e = new Emprunt(u, l);
        e.setDateRetourPrevue(LocalDate.now().plusDays(30));
        return e;
    }

    @Test
    void partialUniqueIndexBlocksSecondActiveLoanForSameBook() {
        Livre livre = livreRepository.save(new Livre("Dune", "Frank Herbert"));
        Utilisateur alice = utilisateurRepository.save(new Utilisateur("Alice", "alice-index@example.com"));
        Utilisateur bob = utilisateurRepository.save(new Utilisateur("Bob", "bob-index@example.com"));

        empruntRepository.saveAndFlush(createEmprunt(alice, livre));

        Emprunt secondEmprunt = createEmprunt(bob, livre);

        assertThatThrownBy(() -> empruntRepository.saveAndFlush(secondEmprunt))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("idx_emprunts_livre_actif_unique");
    }

    @Test
    void partialUniqueIndexStillAllowsReturnedLoanHistoryAndOneActiveLoan() {
        Livre livre = livreRepository.save(new Livre("Foundation", "Isaac Asimov"));
        Utilisateur alice = utilisateurRepository.save(new Utilisateur("Alice", "alice-history@example.com"));
        Utilisateur bob = utilisateurRepository.save(new Utilisateur("Bob", "bob-history@example.com"));

        Emprunt historique = createEmprunt(alice, livre);
        historique.setDateRetour(LocalDate.now());
        empruntRepository.saveAndFlush(historique);

        Emprunt actif = empruntRepository.saveAndFlush(createEmprunt(bob, livre));

        assertThat(actif.getId()).isNotNull();
        assertThat(empruntRepository.count()).isEqualTo(2);
        assertThat(empruntRepository.countByDateRetourIsNull()).isEqualTo(1);
    }

    // ---- Integration tests for new repository queries ----

    @Test
    void findActiveLoans_returnsOnlyUnreturnedLoans() {
        Livre dune = livreRepository.save(new Livre("Dune", "Frank Herbert"));
        Livre fondation = livreRepository.save(new Livre("Fondation", "Isaac Asimov"));
        Utilisateur alice = utilisateurRepository.save(new Utilisateur("Alice", "alice-active@example.com"));

        // Active loan
        empruntRepository.saveAndFlush(createEmprunt(alice, dune));

        // Returned loan
        Emprunt returned = createEmprunt(alice, fondation);
        returned.setDateRetour(LocalDate.now().minusDays(5));
        empruntRepository.saveAndFlush(returned);

        var active = empruntRepository.findActiveLoans();

        assertThat(active).hasSize(1);
        assertThat(active.get(0).getLivre().getTitre()).isEqualTo("Dune");
    }

    @Test
    void findActiveLoansFiltered_filtersByUserName() {
        Livre dune = livreRepository.save(new Livre("Dune", "Frank Herbert"));
        Livre fondation = livreRepository.save(new Livre("Fondation", "Isaac Asimov"));
        Livre neuromancien = livreRepository.save(new Livre("Neuromancien", "William Gibson"));
        Utilisateur alice = utilisateurRepository.save(new Utilisateur("Alice", "alice-filter@example.com"));
        Utilisateur bob = utilisateurRepository.save(new Utilisateur("Bob", "bob-filter@example.com"));

        empruntRepository.saveAndFlush(createEmprunt(alice, dune));
        empruntRepository.saveAndFlush(createEmprunt(bob, fondation));
        empruntRepository.saveAndFlush(createEmprunt(alice, neuromancien));

        var filtered = empruntRepository.findActiveLoansFiltered("%ali%", null);

        assertThat(filtered).hasSize(2);
        assertThat(filtered).allMatch(e -> e.getUtilisateur().getNom().contains("Ali"));
    }

    @Test
    void findHistoryPaged_returnsPaginatedHistory() {
        Livre livre = livreRepository.save(new Livre("Dune", "Frank Herbert"));
        Utilisateur alice = utilisateurRepository.save(new Utilisateur("Alice", "alice-page@example.com"));

        for (int i = 0; i < 25; i++) {
            Livre l = livreRepository.save(new Livre("Book" + i, "Author" + i));
            Emprunt e = createEmprunt(alice, l);
            e.setDateRetour(LocalDate.now().minusDays(i));
            empruntRepository.saveAndFlush(e);
        }

        var page1 = empruntRepository.findHistoryPaged(null, null, null,
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(page1.getContent()).hasSize(10);
        assertThat(page1.getTotalElements()).isEqualTo(25);
        assertThat(page1.getTotalPages()).isEqualTo(3);
    }

    @Test
    void findHistoryPaged_appliesSearchFilter() {
        Livre dune = livreRepository.save(new Livre("Dune", "Frank Herbert"));
        Livre fondation = livreRepository.save(new Livre("Fondation", "Isaac Asimov"));
        Utilisateur alice = utilisateurRepository.save(new Utilisateur("Alice", "alice-search@example.com"));

        Emprunt e1 = createEmprunt(alice, dune);
        e1.setDateRetour(LocalDate.now().minusDays(10));
        empruntRepository.saveAndFlush(e1);

        Emprunt e2 = createEmprunt(alice, fondation);
        e2.setDateRetour(LocalDate.now().minusDays(5));
        empruntRepository.saveAndFlush(e2);

        var page = empruntRepository.findHistoryPaged(null, "%dun%", null,
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getLivre().getTitre()).isEqualTo("Dune");
    }

    @Test
    void countHistoryFiltered_matchesFindHistoryPaged() {
        Livre dune = livreRepository.save(new Livre("Dune", "Frank Herbert"));
        Livre fondation = livreRepository.save(new Livre("Fondation", "Isaac Asimov"));
        Utilisateur alice = utilisateurRepository.save(new Utilisateur("Alice", "alice-count@example.com"));

        Emprunt e1 = createEmprunt(alice, dune);
        e1.setDateRetour(LocalDate.now().minusDays(10));
        empruntRepository.saveAndFlush(e1);

        Emprunt e2 = createEmprunt(alice, fondation);
        e2.setDateRetour(LocalDate.now().minusDays(5));
        empruntRepository.saveAndFlush(e2);

        long count = empruntRepository.countHistoryFiltered(null, "%dun%", null);
        var page = empruntRepository.findHistoryPaged(null, "%dun%", null,
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(count).isEqualTo(page.getTotalElements());
    }
}
