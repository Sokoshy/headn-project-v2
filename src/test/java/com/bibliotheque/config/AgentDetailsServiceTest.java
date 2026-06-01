package com.bibliotheque.config;

import com.bibliotheque.model.Agent;
import com.bibliotheque.model.Role;
import com.bibliotheque.repository.AgentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentDetailsServiceTest {

    @Mock
    private AgentRepository agentRepository;

    private AgentDetailsService service;

    @BeforeEach
    void setUp() {
        service = new AgentDetailsService(agentRepository);
    }

    @Test
    @DisplayName("Email valide (avec espaces et majuscules) retourne UserDetails avec le bon rôle après normalisation")
    void loadUserByUsername_valide_normaliseEmailEtRetourneUserDetails() {
        Agent agent = new Agent("Alice", "alice@bib.fr", "hashedpassword", Role.LIBRARIAN);
        when(agentRepository.findByEmail("alice@bib.fr")).thenReturn(Optional.of(agent));

        UserDetails userDetails = service.loadUserByUsername("  Alice@Bib.Fr  ");

        assertEquals("alice@bib.fr", userDetails.getUsername());
        assertEquals("hashedpassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_LIBRARIAN")));
        verify(agentRepository).findByEmail("alice@bib.fr");
    }

    @Test
    @DisplayName("Agent avec rôle ADMIN reçoit l'autorité ROLE_ADMIN")
    void loadUserByUsername_admin_retourneRoleAdmin() {
        Agent agent = new Agent("Bob", "bob@bib.fr", "hashedpassword", Role.ADMIN);
        when(agentRepository.findByEmail("bob@bib.fr")).thenReturn(Optional.of(agent));

        UserDetails userDetails = service.loadUserByUsername("bob@bib.fr");

        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Email inconnu lève UsernameNotFoundException")
    void loadUserByUsername_inconnu_lanceUsernameNotFoundException() {
        when(agentRepository.findByEmail("unknown@bib.fr")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("unknown@bib.fr"));
    }

    @Test
    @DisplayName("Agent désactivé lève UsernameNotFoundException")
    void loadUserByUsername_desactive_lanceUsernameNotFoundException() {
        Agent agent = new Agent("Bob", "bob@bib.fr", "hashedpassword", Role.ADMIN);
        agent.setActif(false);
        when(agentRepository.findByEmail("bob@bib.fr")).thenReturn(Optional.of(agent));

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("bob@bib.fr"));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Email null lève UsernameNotFoundException")
    void loadUserByUsername_null_lanceUsernameNotFoundException() {
        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername(null));
    }

    @Test
    @DisplayName("Email vide lève UsernameNotFoundException")
    void loadUserByUsername_vide_lanceUsernameNotFoundException() {
        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername(""));
    }
}
