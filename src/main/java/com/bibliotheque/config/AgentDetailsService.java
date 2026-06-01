package com.bibliotheque.config;

import com.bibliotheque.model.Agent;
import com.bibliotheque.repository.AgentRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * Spring Security bridge: looks up an agent by email and exposes the agent
 * identity + role to the SecurityContext. Inactive agents cannot authenticate.
 */
@Service
public class AgentDetailsService implements UserDetailsService {

    private final AgentRepository agentRepository;

    public AgentDetailsService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Identifiants invalides");
        }
        String emailNormalise = email.toLowerCase(Locale.ROOT).trim();
        Agent agent = agentRepository.findByEmail(emailNormalise)
                .orElseThrow(() -> new UsernameNotFoundException("Identifiants invalides"));

        if (!agent.isActif()) {
            throw new UsernameNotFoundException("Identifiants invalides");
        }

        return new User(
                agent.getEmail(),
                agent.getMotDePasse(),
                List.of(new SimpleGrantedAuthority("ROLE_" + agent.getRole().name()))
        );
    }
}