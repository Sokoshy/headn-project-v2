package com.bibliotheque.repository;

import com.bibliotheque.BibliothequeApplication;
import com.bibliotheque.model.Livre;
import com.bibliotheque.support.PostgresIntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = BibliothequeApplication.class)
class LivreRepositoryPostgresTest extends PostgresIntegrationTestBase {

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private EmpruntRepository empruntRepository;

    @BeforeEach
    void cleanDatabase() {
        empruntRepository.deleteAllInBatch();
        livreRepository.deleteAllInBatch();
    }

    @Test
    void save_persisteEtRecupereUnLivre() {
        Livre livre = livreRepository.save(new Livre("Dune", "Frank Herbert"));

        Optional<Livre> retrouve = livreRepository.findById(livre.getId());
        assertThat(retrouve).isPresent();
        assertThat(retrouve.get().getTitre()).isEqualTo("Dune");
        assertThat(retrouve.get().getAuteur()).isEqualTo("Frank Herbert");
        assertThat(retrouve.get().isDisponible()).isTrue();
        assertThat(retrouve.get().getDateCreation()).isNotNull();
    }

    @Test
    void findByTitreContainingIgnoreCase_trouveSansSensibiliteCasse() {
        livreRepository.save(new Livre("Dune", "Frank Herbert"));
        livreRepository.save(new Livre("1984", "George Orwell"));

        List<Livre> resultat = livreRepository.findByTitreContainingIgnoreCase("dun");

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getTitre()).isEqualTo("Dune");
    }

    @Test
    void findByAuteurContainingIgnoreCase_trouveSansSensibiliteCasse() {
        livreRepository.save(new Livre("Dune", "Frank Herbert"));
        livreRepository.save(new Livre("1984", "George Orwell"));

        List<Livre> resultat = livreRepository.findByAuteurContainingIgnoreCase("herbert");

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getAuteur()).isEqualTo("Frank Herbert");
    }

    @Test
    void findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCase_chercheDansLesDeux() {
        livreRepository.save(new Livre("Dune", "Frank Herbert"));
        livreRepository.save(new Livre("1984", "George Orwell"));
        livreRepository.save(new Livre("Le Seigneur des Anneaux", "J.R.R. Tolkien"));

        List<Livre> parTitre = livreRepository.findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCase("dun", "dun");
        assertThat(parTitre).hasSize(1);

        List<Livre> parAuteur = livreRepository.findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCase("orwell", "orwell");
        assertThat(parAuteur).hasSize(1);
    }

    @Test
    void findDisponibles_retourneSeulementLesLivresSansEmpruntActif() {
        Livre disponible = livreRepository.save(new Livre("Dune", "Frank Herbert"));
        disponible.setDisponible(true);
        livreRepository.save(disponible);

        List<Livre> disponibles = livreRepository.findDisponibles();

        assertThat(disponibles).extracting(Livre::getId).contains(disponible.getId());
    }

    @Test
    void countDisponibles_compteLesLivresSansEmpruntActif() {
        livreRepository.save(new Livre("Dune", "Frank Herbert"));
        livreRepository.save(new Livre("1984", "George Orwell"));

        long count = livreRepository.countDisponibles();

        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    @Test
    void existsByTitreAndAuteur_verifieLexistance() {
        livreRepository.save(new Livre("Dune", "Frank Herbert"));

        assertThat(livreRepository.existsByTitreAndAuteur("Dune", "Frank Herbert")).isTrue();
        assertThat(livreRepository.existsByTitreAndAuteur("1984", "George Orwell")).isFalse();
    }
}
