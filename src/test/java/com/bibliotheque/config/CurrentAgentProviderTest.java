package com.bibliotheque.config;

import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.model.Agent;
import com.bibliotheque.model.Role;
import com.bibliotheque.repository.AgentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentAgentProviderTest {

    @Mock
    private AgentRepository agentRepository;

    private CurrentAgentProvider provider;

    @BeforeEach
    void setUp() {
        provider = new CurrentAgentProvider(agentRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Pas d'authentification retourne null")
    void getCurrentAgentOrNull_pasAuth_retourneNull() {
        // Explicite : on vide le contexte pour ce test (au cas où un autre test en aurait posé un).
        SecurityContextHolder.clearContext();

        Agent result = provider.getCurrentAgentOrNull();

        assertNull(result);
    }

    @Test
    @DisplayName("Utilisateur anonyme retourne null")
    void getCurrentAgentOrNull_anonyme_retourneNull() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "anonymousUser", "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

        Agent result = provider.getCurrentAgentOrNull();

        assertNull(result);
    }

    @Test
    @DisplayName("Authentification non confirmée (2-arg ctor) retourne null et ne consulte pas la DB")
    void getCurrentAgentOrNull_authNonConfirmee_retourneNull() {
        // Le constructeur 2-arg de UsernamePasswordAuthenticationToken appelle super.setAuthenticated(false)
        // — cela couvre la branche !auth.isAuthenticated() du provider.
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice@bib.fr", "n/a"));

        Agent result = provider.getCurrentAgentOrNull();

        assertNull(result);
        verify(agentRepository, never()).findByEmail(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("Agent authentifié trouvé retourne l'agent")
    void getCurrentAgentOrNull_agentTrouve_retourneAgent() {
        Agent agent = new Agent("Alice", "alice@bib.fr", "HASH", Role.LIBRARIAN);
        agent.setId(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "alice@bib.fr", "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_LIBRARIAN"))));
        when(agentRepository.findByEmail("alice@bib.fr")).thenReturn(Optional.of(agent));

        Agent result = provider.getCurrentAgentOrNull();

        assertEquals(agent, result);
    }

    @Test
    @DisplayName("Agent authentifié mais absent de la DB retourne null")
    void getCurrentAgentOrNull_agentAbsent_retourneNull() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "ghost@bib.fr", "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_LIBRARIAN"))));
        when(agentRepository.findByEmail("ghost@bib.fr")).thenReturn(Optional.empty());

        Agent result = provider.getCurrentAgentOrNull();

        assertNull(result);
    }

    @Test
    @DisplayName("getCurrentAgent retourne l'agent authentifié trouvé en DB")
    void getCurrentAgent_authentifie_retourneAgent() {
        Agent agent = new Agent("Alice", "alice@bib.fr", "HASH", Role.LIBRARIAN);
        agent.setId(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "alice@bib.fr", "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_LIBRARIAN"))));
        when(agentRepository.findByEmail("alice@bib.fr")).thenReturn(Optional.of(agent));

        Agent result = provider.getCurrentAgent();

        assertEquals(agent, result);
    }

    @Test
    @DisplayName("getCurrentAgent sans authentification lance BusinessException avec code AGENT_ABSENT")
    void getCurrentAgent_pasAuth_lanceBusinessExceptionAvecCodeAgentAbsent() {
        SecurityContextHolder.clearContext();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> provider.getCurrentAgent());
        assertEquals("AGENT_ABSENT", ex.getCode());
        assertTrue(ex.getMessage().contains("authentifié"),
                "Le message devrait mentionner l'absence d'agent authentifié");
    }
}
