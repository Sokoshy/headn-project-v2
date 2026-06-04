package com.bibliotheque.config;

import com.bibliotheque.model.Agent;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Custom {@link UserDetails} implementation that wraps an {@link Agent} entity.
 * This keeps the security layer decoupled from the persistence model while
 * giving Spring Security direct access to agent data through a standard contract.
 */
public class AgentPrincipal implements UserDetails {

    private final Agent agent;

    public AgentPrincipal(Agent agent) {
        this.agent = agent;
    }

    /**
     * Returns the underlying agent entity.
     */
    public Agent getAgent() {
        return agent;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + agent.getRole().name()));
    }

    @Override
    public String getPassword() {
        return agent.getMotDePasse();
    }

    @Override
    public String getUsername() {
        return agent.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return agent.isActif();
    }
}
