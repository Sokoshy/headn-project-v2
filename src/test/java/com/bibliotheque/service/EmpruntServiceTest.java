package com.bibliotheque.service;

import com.bibliotheque.exception.EmpruntDejaRetourneException;
import com.bibliotheque.exception.LivreNonDisponibleException;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.repository.EmpruntRepository;
import com.bibliotheque.repository.LivreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpruntServiceTest {

    @Mock
    private EmpruntRepository empruntRepository;

    @Mock
    private LivreRepository livreRepository;

    @Mock
    private LivreService livreService;

    @Mock
    private UtilisateurService utilisateurService;

    private EmpruntService empruntService;

    @BeforeEach
    void setUp() {
        empruntService = new EmpruntService(empruntRepository, livreRepository, livreService, utilisateurService);
    }

    @Test
    void creer_setsDateRetourPrevue() {
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Livre livre = new Livre("Dune", "Frank Herbert");
        livre.setId(2L);
        LocalDate datePrevue = LocalDate.now().plusDays(30);

        when(utilisateurService.findById(1L)).thenReturn(utilisateur);
        when(livreRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(livre));
        when(empruntRepository.existsByLivreAndDateRetourIsNull(livre)).thenReturn(false);
        when(empruntRepository.saveAndFlush(any(Emprunt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprunt resultat = empruntService.creer(1L, 2L, datePrevue);

        assertThat(resultat.getDateRetourPrevue()).isEqualTo(datePrevue);
    }

    @Test
    void creer_usesLockedBook() {
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Livre livre = new Livre("Dune", "Frank Herbert");
        livre.setId(2L);

        when(utilisateurService.findById(1L)).thenReturn(utilisateur);
        when(livreRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(livre));
        when(empruntRepository.existsByLivreAndDateRetourIsNull(livre)).thenReturn(false);
        when(empruntRepository.saveAndFlush(any(Emprunt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprunt resultat = empruntService.creer(1L, 2L, null);

        assertThat(resultat.getLivre()).isSameAs(livre);
        assertThat(resultat.getUtilisateur()).isSameAs(utilisateur);
        verify(livreRepository).findByIdForUpdate(2L);
    }

    @Test
    void creer_rejectsWhenBookAlreadyHasActiveLoan() {
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Livre livre = new Livre("Dune", "Frank Herbert");
        livre.setId(2L);

        when(utilisateurService.findById(1L)).thenReturn(utilisateur);
        when(livreRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(livre));
        when(empruntRepository.existsByLivreAndDateRetourIsNull(livre)).thenReturn(true);

        assertThatThrownBy(() -> empruntService.creer(1L, 2L, null))
                .isInstanceOf(LivreNonDisponibleException.class)
                .hasMessageContaining("Dune");
    }

    @Test
    void creer_translatesDatabaseRaceToBusinessException() {
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Livre livre = new Livre("Dune", "Frank Herbert");
        livre.setId(2L);

        when(utilisateurService.findById(1L)).thenReturn(utilisateur);
        when(livreRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(livre));
        when(empruntRepository.existsByLivreAndDateRetourIsNull(livre)).thenReturn(false);
        when(empruntRepository.saveAndFlush(any(Emprunt.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate active loan"));

        assertThatThrownBy(() -> empruntService.creer(1L, 2L, null))
                .isInstanceOf(LivreNonDisponibleException.class)
                .hasMessageContaining("Dune");
    }

    @Test
    void effectuerRetour_rejectsAlreadyReturnedLoan() {
        Emprunt emprunt = new Emprunt();
        emprunt.setId(4L);
        emprunt.setDateEmprunt(LocalDate.now().minusDays(10));
        emprunt.setDateRetour(LocalDate.now().minusDays(1));

        when(empruntRepository.findById(4L)).thenReturn(Optional.of(emprunt));

        assertThatThrownBy(() -> empruntService.effectuerRetour(4L))
                .isInstanceOf(EmpruntDejaRetourneException.class);
    }

    @Test
    void effectuerRetour_setsReturnDate() {
        Livre livre = new Livre("Dune", "Frank Herbert");

        Emprunt emprunt = new Emprunt();
        emprunt.setId(4L);
        emprunt.setLivre(livre);
        emprunt.setDateEmprunt(LocalDate.now().minusDays(10));

        when(empruntRepository.findById(4L)).thenReturn(Optional.of(emprunt));
        when(empruntRepository.save(any(Emprunt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprunt resultat = empruntService.effectuerRetour(4L);

        assertThat(resultat.getDateRetour()).isEqualTo(LocalDate.now());
    }

    @Test
    void effectuerRetour_savesReturnedLoanWithoutMutatingBook() {
        Livre livre = new Livre("Dune", "Frank Herbert");

        Emprunt emprunt = new Emprunt();
        emprunt.setId(4L);
        emprunt.setLivre(livre);
        emprunt.setDateEmprunt(LocalDate.now().minusDays(10));

        when(empruntRepository.findById(4L)).thenReturn(Optional.of(emprunt));
        when(empruntRepository.save(any(Emprunt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprunt resultat = empruntService.effectuerRetour(4L);

        assertThat(resultat.getDateRetour()).isEqualTo(LocalDate.now());
    }
}
