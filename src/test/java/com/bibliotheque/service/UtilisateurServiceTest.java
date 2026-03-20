package com.bibliotheque.service;

import com.bibliotheque.exception.EmailDejaUtiliseException;
import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.exception.UtilisateurNotFoundException;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.repository.EmpruntRepository;
import com.bibliotheque.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private EmpruntRepository empruntRepository;

    private UtilisateurService utilisateurService;

    @BeforeEach
    void setUp() {
        utilisateurService = new UtilisateurService(utilisateurRepository, empruntRepository);
    }

    @Test
    void findByEmail_normalizesInputBeforeLookup() {
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        utilisateur.setId(1L);

        when(utilisateurRepository.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(utilisateur));

        Utilisateur resultat = utilisateurService.findByEmail("  Alice@Example.com ");

        assertThat(resultat).isSameAs(utilisateur);
        verify(utilisateurRepository).findByEmail("alice@example.com");
    }

    @Test
    void creer_normalizesEmailBeforeSave() {
        Utilisateur utilisateur = new Utilisateur("Alice", "  Alice@Example.com ");

        when(utilisateurRepository.findByEmail("alice@example.com"))
                .thenReturn(Optional.empty());
        when(utilisateurRepository.save(any(Utilisateur.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Utilisateur resultat = utilisateurService.creer(utilisateur);

        assertThat(resultat.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void creer_rejectsDuplicateNormalizedEmail() {
        Utilisateur utilisateur = new Utilisateur("Alice", " Alice@Example.com ");
        Utilisateur existant = new Utilisateur("Bob", "alice@example.com");
        existant.setId(2L);

        when(utilisateurRepository.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(existant));

        assertThatThrownBy(() -> utilisateurService.creer(utilisateur))
                .isInstanceOf(EmailDejaUtiliseException.class)
                .hasMessageContaining("alice@example.com");
    }

    @Test
    void findByEmail_throwsWhenNormalizedEmailDoesNotExist() {
        when(utilisateurRepository.findByEmail("alice@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> utilisateurService.findByEmail(" Alice@Example.com "))
                .isInstanceOf(UtilisateurNotFoundException.class)
                .hasMessageContaining("Alice@Example.com");
    }

    @Test
    void modifier_allowsSameNormalizedEmailForSameUser() {
        Utilisateur existant = new Utilisateur("Alice", "alice@example.com");
        existant.setId(1L);

        Utilisateur modifie = new Utilisateur("Alice Dupont", "  Alice@Example.com ");

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(utilisateurRepository.save(any(Utilisateur.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Utilisateur resultat = utilisateurService.modifier(1L, modifie);

        assertThat(resultat.getNom()).isEqualTo("Alice Dupont");
        assertThat(resultat.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void modifier_rejectsDuplicateNormalizedEmailFromAnotherUser() {
        Utilisateur existant = new Utilisateur("Alice", "alice@example.com");
        existant.setId(1L);

        Utilisateur modifie = new Utilisateur("Alice", " Bob@Example.com ");
        Utilisateur autreUtilisateur = new Utilisateur("Bob", "bob@example.com");
        autreUtilisateur.setId(2L);

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(utilisateurRepository.findByEmail("bob@example.com"))
                .thenReturn(Optional.of(autreUtilisateur));

        assertThatThrownBy(() -> utilisateurService.modifier(1L, modifie))
                .isInstanceOf(EmailDejaUtiliseException.class)
                .hasMessageContaining("bob@example.com");
    }

    @Test
    void supprimer_rejectsUserWithLoanHistory() {
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        utilisateur.setId(1L);

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(empruntRepository.existsByUtilisateur(utilisateur)).thenReturn(true);

        assertThatThrownBy(() -> utilisateurService.supprimer(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("historique d'emprunts");

        verify(utilisateurRepository, never()).delete(any(Utilisateur.class));
    }

    @Test
    void supprimer_deletesUserWithoutLoanHistory() {
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com");
        utilisateur.setId(1L);

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(empruntRepository.existsByUtilisateur(utilisateur)).thenReturn(false);

        utilisateurService.supprimer(1L);

        verify(utilisateurRepository).delete(utilisateur);
    }
}
