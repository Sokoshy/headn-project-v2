package com.bibliotheque.service;

import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.exception.EmailDejaUtiliseException;
import com.bibliotheque.exception.UtilisateurNotFoundException;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.repository.EmpruntRepository;
import com.bibliotheque.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final EmpruntRepository empruntRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              EmpruntRepository empruntRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.empruntRepository = empruntRepository;
    }

    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }

    public Utilisateur findById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurNotFoundException(id));
    }

    public Utilisateur findByEmail(String email) {
        String emailNormalise = normaliserEmail(email);
        return utilisateurRepository.findByEmail(emailNormalise)
                .orElseThrow(() -> new UtilisateurNotFoundException(email));
    }

    public List<Utilisateur> findByRecherche(String recherche) {
        if (recherche == null || recherche.isBlank()) {
            return findAll();
        }
        return utilisateurRepository.findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(
                recherche.trim(), recherche.trim());
    }

    @Transactional
    public Utilisateur creer(Utilisateur utilisateur) {
        normaliserEmail(utilisateur);
        verifierUniciteEmail(utilisateur.getEmail(), null);
        return utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public Utilisateur modifier(Long id, Utilisateur utilisateurModifie) {
        Utilisateur utilisateurExistant = findById(id);
        normaliserEmail(utilisateurModifie);

        if (!utilisateurExistant.getEmail().equalsIgnoreCase(utilisateurModifie.getEmail())) {
            verifierUniciteEmail(utilisateurModifie.getEmail(), id);
        }

        utilisateurExistant.setNom(utilisateurModifie.getNom());
        utilisateurExistant.setEmail(utilisateurModifie.getEmail());

        return utilisateurRepository.save(utilisateurExistant);
    }

    @Transactional
    public void supprimer(Long id) {
        Utilisateur utilisateur = findById(id);
        if (empruntRepository.existsByUtilisateur(utilisateur)) {
            throw new BusinessException(
                    String.format("Impossible de supprimer l'utilisateur '%s' : un historique d'emprunts lui est associé", utilisateur.getNom()),
                    "SUPPRESSION_IMPOSSIBLE");
        }
        utilisateurRepository.delete(utilisateur);
    }

    public boolean emailExiste(String email) {
        String emailNormalise = normaliserEmail(email);
        if (emailNormalise == null) {
            return false;
        }
        return utilisateurRepository.existsByEmail(emailNormalise);
    }

    public long countTotal() {
        return utilisateurRepository.count();
    }

    private void normaliserEmail(Utilisateur utilisateur) {
        if (utilisateur.getEmail() != null) {
            utilisateur.setEmail(normaliserEmail(utilisateur.getEmail()));
        }
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

        String emailNormalise = normaliserEmail(email);

        boolean emailExiste = utilisateurRepository.findByEmail(emailNormalise)
                .map(u -> !u.getId().equals(idExclu))
                .orElse(false);

        if (emailExiste) {
            throw new EmailDejaUtiliseException(emailNormalise);
        }
    }
}
