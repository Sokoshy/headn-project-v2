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
        empruntRepository.saveAndFlush(new Emprunt(utilisateur, livre));

        assertThatThrownBy(() -> {
            livreRepository.delete(livre);
            livreRepository.flush();
        })
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("emprunts_livre_id_fkey");
    }

    @Test
    void deletingUserWithLoanHistoryFailsBecauseOfRestrictForeignKey() {
        Utilisateur utilisateur = utilisateurRepository.save(new Utilisateur("Bob", "bob-schema@example.com"));
        Livre livre = livreRepository.save(new Livre("1984", "George Orwell"));
        empruntRepository.saveAndFlush(new Emprunt(utilisateur, livre));

        assertThatThrownBy(() -> {
            utilisateurRepository.delete(utilisateur);
            utilisateurRepository.flush();
        })
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("emprunts_utilisateur_id_fkey");
    }
}
