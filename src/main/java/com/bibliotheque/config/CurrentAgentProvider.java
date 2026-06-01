package com.bibliotheque.config;

import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.model.Agent;
import com.bibliotheque.repository.AgentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the currently authenticated agent from the SecurityContext.
 * Used by controllers/services that need to attribute actions to a specific agent.
 */
@Component
public class CurrentAgentProvider {

    private final AgentRepository agentRepository;

    public CurrentAgentProvider(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    /**
     * Returns the currently authenticated agent, or null if no agent is authenticated
     * (e.g. during the /setup flow).
     */
    public Agent getCurrentAgentOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String email = auth.getName();
        return agentRepository.findByEmail(email).orElse(null);
    }

    /**
     * Returns the currently authenticated agent, or throws if no agent is authenticated.
     */
    public Agent getCurrentAgent() {
        Agent agent = getCurrentAgentOrNull();
        if (agent == null) {
            throw new BusinessException("Aucun agent authentifié", "AGENT_ABSENT");
        }
        return agent;
    }
}