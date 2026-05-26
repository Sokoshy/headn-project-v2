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
class SchemaMigrationPostgresTest extends PostgresIntegrationTestBase {

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
    void deletingBookWithLoanHistoryFailsBecauseOfRestrictForeignKey() {
        Utilisateur utilisateur = utilisateurRepository.save(new Utilisateur("Alice", "alice-schema@example.com"));
        Livre livre = livreRepository.save(new Livre("Dune", "Frank Herbert"));
        Emprunt emprunt = new Emprunt(utilisateur, livre);
        emprunt.setDateRetourPrevue(java.time.LocalDate.now().plusDays(30));
        empruntRepository.saveAndFlush(emprunt);

        assertThatThrownBy(() -> {
            livreRepository.delete(livre);
            livreRepository.flush();
        })
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("emprunts_livre_id_fkey");
    }

    @Test
    void v6_backfillsDateRetourPrevueAndMakesItRequired() {
        Utilisateur utilisateur = utilisateurRepository.save(new Utilisateur("Charlie", "charlie-v6@example.com"));
        Livre livre = livreRepository.save(new Livre("Dune", "Frank Herbert"));

        // After V6, saving without dateRetourPrevue should fail (NOT NULL)
        Emprunt sansDatePrevue = new Emprunt(utilisateur, livre);

        assertThatThrownBy(() -> empruntRepository.saveAndFlush(sansDatePrevue))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    void v6_acceptsEmpruntWithDateRetourPrevue() {
        Utilisateur utilisateur = utilisateurRepository.save(new Utilisateur("Diana", "diana-v6@example.com"));
        Livre livre = livreRepository.save(new Livre("1984", "George Orwell"));

        Emprunt avecDatePrevue = new Emprunt(utilisateur, livre);
        avecDatePrevue.setDateRetourPrevue(LocalDate.now().plusDays(30));
        Emprunt saved = empruntRepository.saveAndFlush(avecDatePrevue);

        assertThat(saved.getDateRetourPrevue()).isEqualTo(LocalDate.now().plusDays(30));
    }

    @Test
    void deletingUserWithLoanHistoryFailsBecauseOfRestrictForeignKey() {
        Utilisateur utilisateur = utilisateurRepository.save(new Utilisateur("Bob", "bob-schema@example.com"));
        Livre livre = livreRepository.save(new Livre("1984", "George Orwell"));
        Emprunt emprunt = new Emprunt(utilisateur, livre);
        emprunt.setDateRetourPrevue(java.time.LocalDate.now().plusDays(30));
        empruntRepository.saveAndFlush(emprunt);

        assertThatThrownBy(() -> {
            utilisateurRepository.delete(utilisateur);
            utilisateurRepository.flush();
        })
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("emprunts_utilisateur_id_fkey");
    }
}
