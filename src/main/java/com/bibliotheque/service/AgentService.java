package com.bibliotheque.service;

import com.bibliotheque.exception.AgentNotFoundException;
import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.exception.EmailAgentDejaUtiliseException;
import com.bibliotheque.model.Agent;
import com.bibliotheque.repository.AgentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class AgentService {

    private final AgentRepository agentRepository;
    private final PasswordEncoder passwordEncoder;

    public AgentService(AgentRepository agentRepository, PasswordEncoder passwordEncoder) {
        this.agentRepository = agentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Agent> findAll() {
        return agentRepository.findAll();
    }

    public List<Agent> findActifs() {
        return agentRepository.findByActifTrue();
    }

    public Agent findById(Long id) {
        return agentRepository.findById(id)
                .orElseThrow(() -> new AgentNotFoundException(id));
    }

    public Agent findByEmail(String email) {
        String emailNormalise = normaliserEmail(email);
        return agentRepository.findByEmail(emailNormalise)
                .orElseThrow(() -> new AgentNotFoundException(email));
    }

    public boolean existsAny() {
        return agentRepository.count() > 0;
    }

    @Transactional
    public Agent creer(AgentForm form) {
        validerMotDePasse(form.motDePasse(), form.motDePasseConfirmation());

        String emailNormalise = normaliserEmail(form.email());
        verifierUniciteEmail(emailNormalise, null);

        Agent agent = new Agent(
                form.nom().trim(),
                emailNormalise,
                passwordEncoder.encode(form.motDePasse()),
                form.role()
        );
        if (form.telephone() != null && !form.telephone().isBlank()) {
            agent.setTelephone(form.telephone().trim());
        }
        agent.setActif(true);

        return agentRepository.save(agent);
    }

    @Transactional
    public Agent modifier(Long id, AgentForm form) {
        Agent agent = findById(id);

        String emailNormalise = normaliserEmail(form.email());
        if (!agent.getEmail().equalsIgnoreCase(emailNormalise)) {
            verifierUniciteEmail(emailNormalise, id);
        }

        agent.setNom(form.nom().trim());
        agent.setEmail(emailNormalise);
        agent.setRole(form.role());
        agent.setTelephone(form.telephone() == null || form.telephone().isBlank()
                ? null
                : form.telephone().trim());

        // Password change is optional on update
        if (isMotDePasseFourni(form.motDePasse())) {
            validerMotDePasse(form.motDePasse(), form.motDePasseConfirmation());
            agent.setMotDePasse(passwordEncoder.encode(form.motDePasse()));
        }

        return agentRepository.save(agent);
    }

    @Transactional
    public void desactiver(Long id) {
        Agent agent = findById(id);
        agent.setActif(false);
        agentRepository.save(agent);
    }

    @Transactional
    public void reactiver(Long id) {
        Agent agent = findById(id);
        agent.setActif(true);
        agentRepository.save(agent);
    }

    private void validerMotDePasse(String motDePasse, String confirmation) {
        if (!isMotDePasseFourni(motDePasse)) {
            throw new BusinessException("Le mot de passe est obligatoire", "MOT_DE_PASSE_OBLIGATOIRE");
        }
        if (motDePasse.length() < 8) {
            throw new BusinessException("Le mot de passe doit contenir au moins 8 caractères", "MOT_DE_PASSE_TROP_COURT");
        }
        if (!motDePasse.equals(confirmation)) {
            throw new BusinessException("Les mots de passe ne correspondent pas", "MOT_DE_PASSE_INCOHERENT");
        }
    }

    private boolean isMotDePasseFourni(String motDePasse) {
        return motDePasse != null && !motDePasse.isBlank();
    }

    private String normaliserEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.toLowerCase(Locale.ROOT).trim();
    }

    private void verifierUniciteEmail(String email, Long idExclu) {
        if (email == null || email.isBlank()) {
            return;
        }
        boolean existe = agentRepository.findByEmail(email)
                .map(a -> !a.getId().equals(idExclu))
                .orElse(false);
        if (existe) {
            throw new EmailAgentDejaUtiliseException(email);
        }
    }
}