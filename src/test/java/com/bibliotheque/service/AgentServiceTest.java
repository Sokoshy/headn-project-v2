package com.bibliotheque.service;

import com.bibliotheque.exception.AgentNotFoundException;
import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.exception.EmailAgentDejaUtiliseException;
import com.bibliotheque.model.Agent;
import com.bibliotheque.model.Role;
import com.bibliotheque.repository.AgentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AgentService agentService;

    @BeforeEach
    void setUp() {
        agentService = new AgentService(agentRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Création : le mot de passe est hashé avant sauvegarde")
    void creer_hashesPasswordBeforeSaving() {
        AgentForm form = new AgentForm("Alice", "alice@bib.fr", "secret123", "secret123", Role.ADMIN, null);
        when(agentRepository.findByEmail("alice@bib.fr")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("HASHED");
        when(agentRepository.save(any(Agent.class))).thenAnswer(inv -> inv.getArgument(0));

        Agent resultat = agentService.creer(form);

        ArgumentCaptor<Agent> captor = ArgumentCaptor.forClass(Agent.class);
        verify(agentRepository).save(captor.capture());
        assertThat(captor.getValue().getMotDePasse()).isEqualTo("HASHED");
        assertThat(resultat.getMotDePasse()).isEqualTo("HASHED");
    }

    @Test
    @DisplayName("Création : l'email est normalisé avant sauvegarde")
    void creer_normalizesEmailBeforeSave() {
        AgentForm form = new AgentForm("Alice", "  Alice@Bib.Fr  ", "secret123", "secret123", Role.LIBRARIAN, null);
        when(agentRepository.findByEmail("alice@bib.fr")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("HASHED");
        when(agentRepository.save(any(Agent.class))).thenAnswer(inv -> inv.getArgument(0));

        Agent resultat = agentService.creer(form);

        assertThat(resultat.getEmail()).isEqualTo("alice@bib.fr");
        assertThat(resultat.getRole()).isEqualTo(Role.LIBRARIAN);
        assertThat(resultat.isActif()).isTrue();
    }

    @Test
    @DisplayName("Création : rejette un email en double")
    void creer_rejectsDuplicateEmail() {
        AgentForm form = new AgentForm("Alice", "alice@bib.fr", "secret123", "secret123", Role.LIBRARIAN, null);
        Agent existant = new Agent("Autre", "alice@bib.fr", "HASHED", Role.LIBRARIAN);
        existant.setId(2L);
        when(agentRepository.findByEmail("alice@bib.fr")).thenReturn(Optional.of(existant));

        assertThatThrownBy(() -> agentService.creer(form))
                .isInstanceOf(EmailAgentDejaUtiliseException.class)
                .hasMessageContaining("alice@bib.fr");
        verify(agentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Création : rejette un mot de passe vide")
    void creer_rejectsBlankPassword() {
        AgentForm form = new AgentForm("Alice", "alice@bib.fr", " ", " ", Role.LIBRARIAN, null);

        assertThatThrownBy(() -> agentService.creer(form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("mot de passe");
        verify(agentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Création : rejette si les mots de passe ne correspondent pas")
    void creer_rejectsPasswordMismatch() {
        AgentForm form = new AgentForm("Alice", "alice@bib.fr", "secret123", "different", Role.LIBRARIAN, null);

        assertThatThrownBy(() -> agentService.creer(form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("correspondent");
        verify(agentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Modification : met à jour l'agent et hash le nouveau mot de passe")
    void modifier_updatesAgentAndHashesNewPasswordWhenProvided() {
        Agent existant = new Agent("Alice", "alice@bib.fr", "OLD_HASH", Role.LIBRARIAN);
        existant.setId(1L);
        existant.setActif(true);

        AgentForm form = new AgentForm("Alice Dupont", "alice@bib.fr", "newpass123", "newpass123", Role.ADMIN, "0102");

        when(agentRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(passwordEncoder.encode("newpass123")).thenReturn("NEW_HASH");
        when(agentRepository.save(any(Agent.class))).thenAnswer(inv -> inv.getArgument(0));

        Agent resultat = agentService.modifier(1L, form);

        assertThat(resultat.getNom()).isEqualTo("Alice Dupont");
        assertThat(resultat.getRole()).isEqualTo(Role.ADMIN);
        assertThat(resultat.getTelephone()).isEqualTo("0102");
        assertThat(resultat.getMotDePasse()).isEqualTo("NEW_HASH");
    }

    @Test
    @DisplayName("Modification : conserve le mot de passe existant si non fourni")
    void modifier_keepsExistingPasswordWhenNotProvided() {
        Agent existant = new Agent("Alice", "alice@bib.fr", "OLD_HASH", Role.LIBRARIAN);
        existant.setId(1L);

        AgentForm form = new AgentForm("Alice Dupont", "alice@bib.fr", null, null, Role.LIBRARIAN, null);

        when(agentRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(agentRepository.save(any(Agent.class))).thenAnswer(inv -> inv.getArgument(0));

        Agent resultat = agentService.modifier(1L, form);

        assertThat(resultat.getNom()).isEqualTo("Alice Dupont");
        assertThat(resultat.getMotDePasse()).isEqualTo("OLD_HASH");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Modification : rejette un email déjà utilisé par un autre agent")
    void modifier_rejectsDuplicateEmailFromAnotherAgent() {
        Agent existant = new Agent("Alice", "alice@bib.fr", "OLD_HASH", Role.LIBRARIAN);
        existant.setId(1L);
        Agent autre = new Agent("Bob", "bob@bib.fr", "HASH", Role.LIBRARIAN);
        autre.setId(2L);

        AgentForm form = new AgentForm("Alice", "  Bob@Bib.Fr  ", null, null, Role.LIBRARIAN, null);

        when(agentRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(agentRepository.findByEmail("bob@bib.fr")).thenReturn(Optional.of(autre));

        assertThatThrownBy(() -> agentService.modifier(1L, form))
                .isInstanceOf(EmailAgentDejaUtiliseException.class);
    }

    @Test
    @DisplayName("Modification : autorise le même email pour le même agent")
    void modifier_allowsSameEmailForSameAgent() {
        Agent existant = new Agent("Alice", "alice@bib.fr", "OLD_HASH", Role.LIBRARIAN);
        existant.setId(1L);
        AgentForm form = new AgentForm("Alice Dupont", "  Alice@Bib.Fr  ", null, null, Role.LIBRARIAN, null);

        when(agentRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(agentRepository.save(any(Agent.class))).thenAnswer(inv -> inv.getArgument(0));

        Agent resultat = agentService.modifier(1L, form);

        assertThat(resultat.getEmail()).isEqualTo("alice@bib.fr");
        assertThat(resultat.getNom()).isEqualTo("Alice Dupont");
    }

    @Test
    @DisplayName("Désactivation : passe actif à false")
    void desactiver_setsActifFalse() {
        Agent existant = new Agent("Alice", "alice@bib.fr", "HASH", Role.LIBRARIAN);
        existant.setId(1L);
        existant.setActif(true);

        when(agentRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(agentRepository.save(any(Agent.class))).thenAnswer(inv -> inv.getArgument(0));

        agentService.desactiver(1L);

        assertThat(existant.isActif()).isFalse();
    }

    @Test
    @DisplayName("Réactivation : passe actif à true")
    void reactiver_setsActifTrue() {
        Agent existant = new Agent("Alice", "alice@bib.fr", "HASH", Role.LIBRARIAN);
        existant.setId(1L);
        existant.setActif(false);

        when(agentRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(agentRepository.save(any(Agent.class))).thenAnswer(inv -> inv.getArgument(0));

        agentService.reactiver(1L);

        assertThat(existant.isActif()).isTrue();
    }

    @Test
    @DisplayName("findById : lance une exception si l'agent n'existe pas")
    void findById_throwsWhenMissing() {
        when(agentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agentService.findById(99L))
                .isInstanceOf(AgentNotFoundException.class);
    }

    @Test
    @DisplayName("findByEmail : normalise l'email avant recherche")
    void findByEmail_normalizesBeforeLookup() {
        Agent existant = new Agent("Alice", "alice@bib.fr", "HASH", Role.LIBRARIAN);
        existant.setId(1L);
        when(agentRepository.findByEmail("alice@bib.fr")).thenReturn(Optional.of(existant));

        Agent resultat = agentService.findByEmail("  Alice@Bib.Fr  ");

        assertThat(resultat).isSameAs(existant);
    }

    @Test
    @DisplayName("findByEmail : lance une exception si l'email n'existe pas")
    void findByEmail_throwsWhenMissing() {
        when(agentRepository.findByEmail("ghost@bib.fr")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agentService.findByEmail("ghost@bib.fr"))
                .isInstanceOf(AgentNotFoundException.class);
    }

    @Test
    @DisplayName("existsAny : retourne true si des agents existent")
    void existsAny_returnsRepositoryHasAny() {
        when(agentRepository.count()).thenReturn(0L);
        assertThat(agentService.existsAny()).isFalse();

        when(agentRepository.count()).thenReturn(1L);
        assertThat(agentService.existsAny()).isTrue();
    }
}