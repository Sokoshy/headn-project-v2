package com.bibliotheque.service;

import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.exception.DuplicateResourceException;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Livre;
import com.bibliotheque.repository.EmpruntRepository;
import com.bibliotheque.repository.LivreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LivreServiceTest {

    @Mock
    private LivreRepository livreRepository;

    @Mock
    private EmpruntRepository empruntRepository;

    private LivreService livreService;

    @BeforeEach
    void setUp() {
        livreService = new LivreService(livreRepository, empruntRepository);
    }

    @Test
    void creer_normalizesTitreAndAuteurBeforeSave() {
        Livre livre = new Livre("  Dune  ", "  Frank Herbert  ");

        when(livreRepository.findByTitreContainingIgnoreCase("Dune"))
                .thenReturn(List.of());
        when(livreRepository.save(any(Livre.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Livre resultat = livreService.creer(livre);

        assertThat(resultat.getTitre()).isEqualTo("Dune");
        assertThat(resultat.getAuteur()).isEqualTo("Frank Herbert");
        assertThat(resultat.isDisponible()).isTrue();
    }

    @Test
    void modifier_allowsSameNormalizedTitreAuteurForSameBook() {
        Livre existant = new Livre("Dune", "Frank Herbert");
        existant.setId(1L);

        Livre livreModifie = new Livre("  Dune  ", " Frank Herbert ");

        when(livreRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(livreRepository.findByTitreContainingIgnoreCase("Dune"))
                .thenReturn(List.of(existant));
        when(livreRepository.save(any(Livre.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Livre resultat = livreService.modifier(1L, livreModifie);

        assertThat(resultat.getTitre()).isEqualTo("Dune");
        assertThat(resultat.getAuteur()).isEqualTo("Frank Herbert");
    }

    @Test
    void creer_rejectsDuplicateAfterNormalization() {
        Livre livre = new Livre("  Dune  ", " Frank Herbert ");
        Livre existant = new Livre("Dune", "Frank Herbert");
        existant.setId(2L);

        when(livreRepository.findByTitreContainingIgnoreCase("Dune"))
                .thenReturn(List.of(existant));

        assertThatThrownBy(() -> livreService.creer(livre))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Dune")
                .hasMessageContaining("Frank Herbert");
    }

    @Test
    void modifier_rejectsDuplicateNormalizedTitreAuteurFromAnotherBook() {
        Livre existant = new Livre("Dune Messiah", "Frank Herbert");
        existant.setId(1L);

        Livre doublon = new Livre("Dune", "Frank Herbert");
        doublon.setId(2L);

        Livre livreModifie = new Livre("  Dune  ", " Frank Herbert ");

        when(livreRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(livreRepository.findByTitreContainingIgnoreCase("Dune"))
                .thenReturn(List.of(doublon));

        assertThatThrownBy(() -> livreService.modifier(1L, livreModifie))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Dune")
                .hasMessageContaining("Frank Herbert");
    }

    @Test
    void supprimer_rejectsBookWithLoanHistoryEvenIfDisponibleFlagIsTrue() {
        Livre livre = new Livre("Dune", "Frank Herbert");
        livre.setId(1L);
        livre.setDisponible(true);

        when(livreRepository.findById(1L)).thenReturn(Optional.of(livre));
        when(empruntRepository.existsByLivre(livre)).thenReturn(true);

        assertThatThrownBy(() -> livreService.supprimer(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("historique d'emprunts");

        verify(empruntRepository).existsByLivre(livre);
        verify(livreRepository, never()).delete(any(Livre.class));
    }

    @Test
    void supprimer_deletesBookWithoutLoanHistory() {
        Livre livre = new Livre("Dune", "Frank Herbert");
        livre.setId(1L);

        when(livreRepository.findById(1L)).thenReturn(Optional.of(livre));
        when(empruntRepository.existsByLivre(livre)).thenReturn(false);

        livreService.supprimer(1L);

        verify(livreRepository).delete(livre);
    }

    @Test
    void estDisponible_usesActiveLoanStateInsteadOfDisponibleFlag() {
        Livre livre = new Livre("Dune", "Frank Herbert");
        livre.setId(1L);
        livre.setDisponible(true);

        when(livreRepository.findById(1L)).thenReturn(Optional.of(livre));
        when(empruntRepository.existsByLivreIdAndDateRetourIsNull(1L)).thenReturn(true);

        boolean disponible = livreService.estDisponible(1L);

        assertThat(disponible).isFalse();
    }

    @Test
    void findDisponibles_delegatesToDerivedAvailabilityQuery() {
        Livre disponible = new Livre("Dune", "Frank Herbert");

        when(livreRepository.findDisponibles()).thenReturn(List.of(disponible));

        List<Livre> resultat = livreService.findDisponibles();

        assertThat(resultat).containsExactly(disponible);
        verify(livreRepository).findDisponibles();
    }
}
