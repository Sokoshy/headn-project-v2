package com.bibliotheque.service;

import com.bibliotheque.exception.EmpruntDejaRetourneException;
import com.bibliotheque.exception.LivreNonDisponibleException;
import com.bibliotheque.model.Agent;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Role;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.repository.EmpruntRepository;
import com.bibliotheque.repository.LivreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.never;
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

    @Mock
    private AuditService auditService;

    private EmpruntService empruntService;

    @BeforeEach
    void setUp() {
        empruntService = new EmpruntService(empruntRepository, livreRepository, livreService,
                utilisateurService, auditService);
    }

    @Test
    @DisplayName("Création : définit la date de retour prévue")
    void creer_setsDateRetourPrevue() {
        Agent agent = agent();
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Livre livre = new Livre("Dune", "Frank Herbert");
        livre.setId(2L);
        LocalDate datePrevue = LocalDate.now().plusDays(30);

        when(utilisateurService.findById(1L)).thenReturn(utilisateur);
        when(livreRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(livre));
        when(empruntRepository.existsByLivreAndDateRetourIsNull(livre)).thenReturn(false);
        when(empruntRepository.saveAndFlush(any(Emprunt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprunt resultat = empruntService.creer(1L, 2L, datePrevue, agent);

        assertThat(resultat.getDateRetourPrevue()).isEqualTo(datePrevue);
    }

    @Test
    @DisplayName("Création : utilise le verrouillage du livre")
    void creer_usesLockedBook() {
        Agent agent = agent();
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Livre livre = new Livre("Dune", "Frank Herbert");
        livre.setId(2L);
        LocalDate datePrevue = LocalDate.now().plusDays(30);

        when(utilisateurService.findById(1L)).thenReturn(utilisateur);
        when(livreRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(livre));
        when(empruntRepository.existsByLivreAndDateRetourIsNull(livre)).thenReturn(false);
        when(empruntRepository.saveAndFlush(any(Emprunt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprunt resultat = empruntService.creer(1L, 2L, datePrevue, agent);

        assertThat(resultat.getLivre()).isSameAs(livre);
        assertThat(resultat.getUtilisateur()).isSameAs(utilisateur);
        verify(livreRepository).findByIdForUpdate(2L);
    }

    @Test
    @DisplayName("Création : rejette si le livre a déjà un emprunt actif")
    void creer_rejectsWhenBookAlreadyHasActiveLoan() {
        Agent agent = agent();
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Livre livre = new Livre("Dune", "Frank Herbert");
        livre.setId(2L);
        LocalDate datePrevue = LocalDate.now().plusDays(30);

        when(utilisateurService.findById(1L)).thenReturn(utilisateur);
        when(livreRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(livre));
        when(empruntRepository.existsByLivreAndDateRetourIsNull(livre)).thenReturn(true);

        assertThatThrownBy(() -> empruntService.creer(1L, 2L, datePrevue, agent))
                .isInstanceOf(LivreNonDisponibleException.class)
                .hasMessageContaining("Dune");
    }

    @Test
    @DisplayName("Création : rejette si la date de retour prévue est nulle")
    void creer_rejectsWhenDateRetourPrevueIsNull() {
        assertThatThrownBy(() -> empruntService.creer(1L, 2L, null, agent()))
                .isInstanceOf(com.bibliotheque.exception.DateRetourPrevueObligatoireException.class)
                .hasMessage("La date de retour prévue est obligatoire.");
    }

    @Test
    @DisplayName("Création : rejette si la date de retour prévue est dans le passé")
    void creer_rejectsWhenDateRetourPrevueIsInThePast() {
        assertThatThrownBy(() -> empruntService.creer(1L, 2L, LocalDate.now().minusDays(1), agent()))
                .isInstanceOf(com.bibliotheque.exception.DateRetourPrevueDansLePasseException.class)
                .hasMessage("La date de retour prévue doit être aujourd'hui ou une date future.");
    }

    @Test
    @DisplayName("Création : accepte une date de retour prévue égale à aujourd'hui")
    void creer_acceptsDateRetourPrevueEqualToToday() {
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Livre livre = new Livre("Dune", "Frank Herbert");
        livre.setId(2L);
        LocalDate today = LocalDate.now();

        when(utilisateurService.findById(1L)).thenReturn(utilisateur);
        when(livreRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(livre));
        when(empruntRepository.existsByLivreAndDateRetourIsNull(livre)).thenReturn(false);
        when(empruntRepository.saveAndFlush(any(Emprunt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprunt resultat = empruntService.creer(1L, 2L, today, agent());

        assertThat(resultat.getDateRetourPrevue()).isEqualTo(today);
    }

    @Test
    @DisplayName("Création : traduit une race condition DB en exception métier")
    void creer_translatesDatabaseRaceToBusinessException() {
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Livre livre = new Livre("Dune", "Frank Herbert");
        livre.setId(2L);
        LocalDate datePrevue = LocalDate.now().plusDays(30);

        when(utilisateurService.findById(1L)).thenReturn(utilisateur);
        when(livreRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(livre));
        when(empruntRepository.existsByLivreAndDateRetourIsNull(livre)).thenReturn(false);
        when(empruntRepository.saveAndFlush(any(Emprunt.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate active loan"));

        assertThatThrownBy(() -> empruntService.creer(1L, 2L, datePrevue, agent()))
                .isInstanceOf(LivreNonDisponibleException.class)
                .hasMessageContaining("Dune");
    }

    @Test
    @DisplayName("Création : enregistre l'audit quand l'agent est fourni")
    void creer_recordsAuditCreationWhenAgentProvided() {
        Agent agent = agent();
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Livre livre = new Livre("Dune", "Frank Herbert");
        livre.setId(2L);
        LocalDate datePrevue = LocalDate.now().plusDays(30);

        when(utilisateurService.findById(1L)).thenReturn(utilisateur);
        when(livreRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(livre));
        when(empruntRepository.existsByLivreAndDateRetourIsNull(livre)).thenReturn(false);
        when(empruntRepository.saveAndFlush(any(Emprunt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprunt resultat = empruntService.creer(1L, 2L, datePrevue, agent);

        verify(auditService).enregistrerCreation(resultat, agent);
    }

    @Test
    @DisplayName("Création : ignore l'audit quand l'agent est null")
    void creer_skipsAuditWhenAgentIsNull() {
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        Livre livre = new Livre("Dune", "Frank Herbert");
        livre.setId(2L);
        LocalDate datePrevue = LocalDate.now().plusDays(30);

        when(utilisateurService.findById(1L)).thenReturn(utilisateur);
        when(livreRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(livre));
        when(empruntRepository.existsByLivreAndDateRetourIsNull(livre)).thenReturn(false);
        when(empruntRepository.saveAndFlush(any(Emprunt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        empruntService.creer(1L, 2L, datePrevue, null);

        verify(auditService, never()).enregistrerCreation(any(), any());
    }

    @Test
    @DisplayName("Retour : rejette un emprunt déjà retourné")
    void effectuerRetour_rejectsAlreadyReturnedLoan() {
        Emprunt emprunt = new Emprunt();
        emprunt.setId(4L);
        emprunt.setDateEmprunt(LocalDate.now().minusDays(10));
        emprunt.setDateRetour(LocalDate.now().minusDays(1));

        when(empruntRepository.findByIdWithDetails(4L)).thenReturn(Optional.of(emprunt));

        assertThatThrownBy(() -> empruntService.effectuerRetour(4L, agent()))
                .isInstanceOf(EmpruntDejaRetourneException.class);
    }

    @Test
    @DisplayName("Retour : définit la date de retour")
    void effectuerRetour_setsReturnDate() {
        Livre livre = new Livre("Dune", "Frank Herbert");

        Emprunt emprunt = new Emprunt();
        emprunt.setId(4L);
        emprunt.setLivre(livre);
        emprunt.setDateEmprunt(LocalDate.now().minusDays(10));

        when(empruntRepository.findByIdWithDetails(4L)).thenReturn(Optional.of(emprunt));
        when(empruntRepository.save(any(Emprunt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprunt resultat = empruntService.effectuerRetour(4L, agent());

        assertThat(resultat.getDateRetour()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("Retour : enregistre l'audit quand l'agent est fourni")
    void effectuerRetour_recordsAuditReturnWhenAgentProvided() {
        Agent agent = agent();
        Livre livre = new Livre("Dune", "Frank Herbert");

        Emprunt emprunt = new Emprunt();
        emprunt.setId(4L);
        emprunt.setLivre(livre);
        emprunt.setDateEmprunt(LocalDate.now().minusDays(10));

        when(empruntRepository.findByIdWithDetails(4L)).thenReturn(Optional.of(emprunt));
        when(empruntRepository.save(any(Emprunt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprunt resultat = empruntService.effectuerRetour(4L, agent);

        verify(auditService).enregistrerRetour(resultat, agent);
    }

    @Test
    @DisplayName("Retour : ignore l'audit quand l'agent est null")
    void effectuerRetour_skipsAuditWhenAgentIsNull() {
        Livre livre = new Livre("Dune", "Frank Herbert");

        Emprunt emprunt = new Emprunt();
        emprunt.setId(4L);
        emprunt.setLivre(livre);
        emprunt.setDateEmprunt(LocalDate.now().minusDays(10));

        when(empruntRepository.findByIdWithDetails(4L)).thenReturn(Optional.of(emprunt));
        when(empruntRepository.save(any(Emprunt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        empruntService.effectuerRetour(4L, null);

        verify(auditService, never()).enregistrerRetour(any(), any());
    }

    // ---- corrigerDateRetourPrevue tests ----

    @Test
    @DisplayName("Correction date : met à jour la date sur un emprunt actif")
    void corrigerDateRetourPrevue_updatesDateOnActiveLoan() {
        Emprunt emprunt = new Emprunt();
        emprunt.setId(1L);
        emprunt.setDateEmprunt(LocalDate.now().minusDays(10));
        emprunt.setDateRetourPrevue(LocalDate.now().plusDays(20));

        LocalDate nouvelleDate = LocalDate.now().plusDays(45);

        when(empruntRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(emprunt));
        when(empruntRepository.save(any(Emprunt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprunt resultat = empruntService.corrigerDateRetourPrevue(1L, nouvelleDate);

        assertThat(resultat.getDateRetourPrevue()).isEqualTo(nouvelleDate);
    }

    @Test
    @DisplayName("Correction date : rejette un emprunt terminé")
    void corrigerDateRetourPrevue_rejectsCompletedLoan() {
        Emprunt emprunt = new Emprunt();
        emprunt.setId(1L);
        emprunt.setDateEmprunt(LocalDate.now().minusDays(60));
        emprunt.setDateRetour(LocalDate.now().minusDays(30));
        emprunt.setDateRetourPrevue(LocalDate.now().minusDays(30));

        when(empruntRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(emprunt));

        assertThatThrownBy(() -> empruntService.corrigerDateRetourPrevue(1L, LocalDate.now().plusDays(30)))
                .isInstanceOf(EmpruntDejaRetourneException.class);
    }

    @Test
    @DisplayName("Correction date : rejette une date nulle")
    void corrigerDateRetourPrevue_rejectsNullDate() {
        assertThatThrownBy(() -> empruntService.corrigerDateRetourPrevue(1L, null))
                .isInstanceOf(com.bibliotheque.exception.DateRetourPrevueObligatoireException.class);
    }

    @Test
    @DisplayName("Correction date : rejette une date dans le passé")
    void corrigerDateRetourPrevue_rejectsPastDate() {
        assertThatThrownBy(() -> empruntService.corrigerDateRetourPrevue(1L, LocalDate.now().minusDays(1)))
                .isInstanceOf(com.bibliotheque.exception.DateRetourPrevueDansLePasseException.class);
    }

    @Test
    @DisplayName("Correction date : accepte la date d'aujourd'hui")
    void corrigerDateRetourPrevue_acceptsToday() {
        Emprunt emprunt = new Emprunt();
        emprunt.setId(1L);
        emprunt.setDateEmprunt(LocalDate.now().minusDays(10));
        emprunt.setDateRetourPrevue(LocalDate.now().plusDays(20));

        when(empruntRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(emprunt));
        when(empruntRepository.save(any(Emprunt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprunt resultat = empruntService.corrigerDateRetourPrevue(1L, LocalDate.now());

        assertThat(resultat.getDateRetourPrevue()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("Retour : sauvegarde le retour sans modifier le livre")
    void effectuerRetour_savesReturnedLoanWithoutMutatingBook() {
        Livre livre = new Livre("Dune", "Frank Herbert");

        Emprunt emprunt = new Emprunt();
        emprunt.setId(4L);
        emprunt.setLivre(livre);
        emprunt.setDateEmprunt(LocalDate.now().minusDays(10));

        when(empruntRepository.findByIdWithDetails(4L)).thenReturn(Optional.of(emprunt));
        when(empruntRepository.save(any(Emprunt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprunt resultat = empruntService.effectuerRetour(4L, agent());

        assertThat(resultat.getDateRetour()).isEqualTo(LocalDate.now());
    }

    private Agent agent() {
        Agent agent = new Agent("Alice", "alice@bib.fr", "HASH", Role.LIBRARIAN);
        agent.setId(1L);
        return agent;
    }
}