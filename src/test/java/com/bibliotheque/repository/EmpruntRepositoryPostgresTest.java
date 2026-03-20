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
    void partialUniqueIndexBlocksSecondActiveLoanForSameBook() {
        Livre livre = livreRepository.save(new Livre("Dune", "Frank Herbert"));
        Utilisateur alice = utilisateurRepository.save(new Utilisateur("Alice", "alice-index@example.com"));
        Utilisateur bob = utilisateurRepository.save(new Utilisateur("Bob", "bob-index@example.com"));

        empruntRepository.saveAndFlush(new Emprunt(alice, livre));

        Emprunt secondEmprunt = new Emprunt(bob, livre);

        assertThatThrownBy(() -> empruntRepository.saveAndFlush(secondEmprunt))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("idx_emprunts_livre_actif_unique");
    }

    @Test
    void partialUniqueIndexStillAllowsReturnedLoanHistoryAndOneActiveLoan() {
        Livre livre = livreRepository.save(new Livre("Foundation", "Isaac Asimov"));
        Utilisateur alice = utilisateurRepository.save(new Utilisateur("Alice", "alice-history@example.com"));
        Utilisateur bob = utilisateurRepository.save(new Utilisateur("Bob", "bob-history@example.com"));

        Emprunt historique = new Emprunt(alice, livre);
        historique.setDateRetour(LocalDate.now());
        empruntRepository.saveAndFlush(historique);

        Emprunt actif = empruntRepository.saveAndFlush(new Emprunt(bob, livre));

        assertThat(actif.getId()).isNotNull();
        assertThat(empruntRepository.count()).isEqualTo(2);
        assertThat(empruntRepository.countByDateRetourIsNull()).isEqualTo(1);
    }
}
