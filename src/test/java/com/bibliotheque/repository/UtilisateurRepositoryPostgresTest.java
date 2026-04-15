package com.bibliotheque.repository;

import com.bibliotheque.BibliothequeApplication;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.support.PostgresIntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = BibliothequeApplication.class)
class UtilisateurRepositoryPostgresTest extends PostgresIntegrationTestBase {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private EmpruntRepository empruntRepository;

    @BeforeEach
    void cleanDatabase() {
        empruntRepository.deleteAllInBatch();
        utilisateurRepository.deleteAllInBatch();
    }

    @Test
    void save_persisteEtRecupereUnUtilisateur() {
        Utilisateur utilisateur = utilisateurRepository.save(new Utilisateur("Alice", "alice@example.com"));

        Optional<Utilisateur> retrouve = utilisateurRepository.findById(utilisateur.getId());
        assertThat(retrouve).isPresent();
        assertThat(retrouve.get().getNom()).isEqualTo("Alice");
        assertThat(retrouve.get().getEmail()).isEqualTo("alice@example.com");
        assertThat(retrouve.get().getDateInscription()).isNotNull();
    }

    @Test
    void findByEmail_trouveParEmail() {
        utilisateurRepository.save(new Utilisateur("Alice", "alice@example.com"));

        Optional<Utilisateur> resultat = utilisateurRepository.findByEmail("alice@example.com");

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getNom()).isEqualTo("Alice");
    }

    @Test
    void findByEmail_emailInexistant_retourneEmpty() {
        assertThat(utilisateurRepository.findByEmail("inexistant@example.com")).isEmpty();
    }

    @Test
    void existsByEmail_verifieExistence() {
        utilisateurRepository.save(new Utilisateur("Alice", "alice@example.com"));

        assertThat(utilisateurRepository.existsByEmail("alice@example.com")).isTrue();
        assertThat(utilisateurRepository.existsByEmail("bob@example.com")).isFalse();
    }

    @Test
    void uniqueConstraint_emailDuplique_leveUneException() {
        utilisateurRepository.save(new Utilisateur("Alice", "alice@example.com"));

        assertThatThrownBy(() -> {
            utilisateurRepository.saveAndFlush(new Utilisateur("Bob", "alice@example.com"));
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByNomContainingIgnoreCase_trouveSansSensibiliteCasse() {
        utilisateurRepository.save(new Utilisateur("Alice Dupont", "alice@example.com"));
        utilisateurRepository.save(new Utilisateur("Bob Martin", "bob@example.com"));

        List<Utilisateur> resultat = utilisateurRepository.findByNomContainingIgnoreCase("dupont");

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getNom()).isEqualTo("Alice Dupont");
    }

    @Test
    void findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase_chercheDansLesDeux() {
        utilisateurRepository.save(new Utilisateur("Alice", "alice@example.com"));
        utilisateurRepository.save(new Utilisateur("Bob", "bob@example.com"));

        List<Utilisateur> parNom = utilisateurRepository.findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase("alice", "alice");
        assertThat(parNom).hasSize(1);

        List<Utilisateur> parEmail = utilisateurRepository.findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase("bob@", "bob@");
        assertThat(parEmail).hasSize(1);
    }
}
