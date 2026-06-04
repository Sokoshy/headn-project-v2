package com.bibliotheque.config;

import com.bibliotheque.model.Agent;
import com.bibliotheque.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentPrincipalTest {

    @Test
    @DisplayName("getUsername() returns the agent's email")
    void getUsername_returnsEmail() {
        Agent agent = new Agent("Alice", "alice@bib.fr", "hashedpassword", Role.LIBRARIAN);
        AgentPrincipal principal = new AgentPrincipal(agent);

        assertEquals("alice@bib.fr", principal.getUsername());
    }

    @Test
    @DisplayName("getPassword() returns the agent's hashed password")
    void getPassword_returnsHashedPassword() {
        Agent agent = new Agent("Alice", "alice@bib.fr", "hashedpassword", Role.LIBRARIAN);
        AgentPrincipal principal = new AgentPrincipal(agent);

        assertEquals("hashedpassword", principal.getPassword());
    }

    @Test
    @DisplayName("getAuthorities() returns ROLE_LIBRARIAN for a LIBRARIAN agent")
    void getAuthorities_librarian() {
        Agent agent = new Agent("Alice", "alice@bib.fr", "hashedpassword", Role.LIBRARIAN);
        AgentPrincipal principal = new AgentPrincipal(agent);

        assertEquals(1, principal.getAuthorities().size());
        assertTrue(principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_LIBRARIAN")));
    }

    @Test
    @DisplayName("getAuthorities() returns ROLE_ADMIN for an ADMIN agent")
    void getAuthorities_admin() {
        Agent agent = new Agent("Bob", "bob@bib.fr", "hashedpassword", Role.ADMIN);
        AgentPrincipal principal = new AgentPrincipal(agent);

        assertEquals(1, principal.getAuthorities().size());
        assertTrue(principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("isEnabled() returns true when agent is active")
    void isEnabled_activeAgent() {
        Agent agent = new Agent("Alice", "alice@bib.fr", "hashedpassword", Role.LIBRARIAN);
        agent.setActif(true);
        AgentPrincipal principal = new AgentPrincipal(agent);

        assertTrue(principal.isEnabled());
    }

    @Test
    @DisplayName("isEnabled() returns false when agent is inactive")
    void isEnabled_inactiveAgent() {
        Agent agent = new Agent("Alice", "alice@bib.fr", "hashedpassword", Role.LIBRARIAN);
        agent.setActif(false);
        AgentPrincipal principal = new AgentPrincipal(agent);

        assertEquals(false, principal.isEnabled());
    }

    @Test
    @DisplayName("isAccountNonExpired() always returns true")
    void isAccountNonExpired_alwaysTrue() {
        Agent agent = new Agent("Alice", "alice@bib.fr", "hashedpassword", Role.LIBRARIAN);
        AgentPrincipal principal = new AgentPrincipal(agent);

        assertTrue(principal.isAccountNonExpired());
    }

    @Test
    @DisplayName("isAccountNonLocked() always returns true")
    void isAccountNonLocked_alwaysTrue() {
        Agent agent = new Agent("Alice", "alice@bib.fr", "hashedpassword", Role.LIBRARIAN);
        AgentPrincipal principal = new AgentPrincipal(agent);

        assertTrue(principal.isAccountNonLocked());
    }

    @Test
    @DisplayName("isCredentialsNonExpired() always returns true")
    void isCredentialsNonExpired_alwaysTrue() {
        Agent agent = new Agent("Alice", "alice@bib.fr", "hashedpassword", Role.LIBRARIAN);
        AgentPrincipal principal = new AgentPrincipal(agent);

        assertTrue(principal.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("getAgent() returns the wrapped agent")
    void getAgent_returnsWrappedAgent() {
        Agent agent = new Agent("Alice", "alice@bib.fr", "hashedpassword", Role.LIBRARIAN);
        AgentPrincipal principal = new AgentPrincipal(agent);

        assertSame(agent, principal.getAgent());
    }
}
