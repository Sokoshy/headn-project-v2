package com.bibliotheque.service;

import com.bibliotheque.BibliothequeApplication;
import com.bibliotheque.exception.LivreNonDisponibleException;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.repository.EmpruntRepository;
import com.bibliotheque.repository.LivreRepository;
import com.bibliotheque.repository.UtilisateurRepository;
import com.bibliotheque.support.PostgresIntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = BibliothequeApplication.class)
class EmpruntServicePostgresTest extends PostgresIntegrationTestBase {

    @Autowired
    private EmpruntService empruntService;

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
    void createAndReturnLoanWorkWithRealPostgresConstraints() {
        Utilisateur utilisateur = utilisateurRepository.save(new Utilisateur("Alice", "alice-service@example.com"));
        Livre livre = livreRepository.save(new Livre("Dune", "Frank Herbert"));

        Emprunt emprunt = empruntService.creer(utilisateur.getId(), livre.getId());

        Livre livreApresCreation = livreRepository.findById(livre.getId()).orElseThrow();
        assertThat(emprunt.getId()).isNotNull();
        assertThat(livreApresCreation.isDisponible()).isFalse();
        assertThat(livreRepository.findDisponibles()).isEmpty();
        assertThat(empruntRepository.countByDateRetourIsNull()).isEqualTo(1);

        assertThatThrownBy(() -> empruntService.creer(utilisateur.getId(), livre.getId()))
                .isInstanceOf(LivreNonDisponibleException.class);

        empruntService.effectuerRetour(emprunt.getId());

        Livre livreApresRetour = livreRepository.findById(livre.getId()).orElseThrow();
        assertThat(livreApresRetour.isDisponible()).isTrue();
        assertThat(livreRepository.findDisponibles()).extracting(Livre::getId).contains(livre.getId());
        assertThat(empruntRepository.countByDateRetourIsNull()).isZero();
    }
}
