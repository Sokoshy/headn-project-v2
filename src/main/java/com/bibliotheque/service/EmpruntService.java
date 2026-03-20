package com.bibliotheque.service;

import com.bibliotheque.exception.EmpruntDejaRetourneException;
import com.bibliotheque.exception.EmpruntNotFoundException;
import com.bibliotheque.exception.LivreNonDisponibleException;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.repository.EmpruntRepository;
import com.bibliotheque.repository.LivreRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EmpruntService {

    private final EmpruntRepository empruntRepository;
    private final LivreRepository livreRepository;
    private final LivreService livreService;
    private final UtilisateurService utilisateurService;

    public EmpruntService(EmpruntRepository empruntRepository,
                          LivreRepository livreRepository,
                          LivreService livreService,
                          UtilisateurService utilisateurService) {
        this.empruntRepository = empruntRepository;
        this.livreRepository = livreRepository;
        this.livreService = livreService;
        this.utilisateurService = utilisateurService;
    }

    public List<Emprunt> findAll() {
        return empruntRepository.findAllWithDetails();
    }

    public Emprunt findById(Long id) {
        return empruntRepository.findById(id)
                .orElseThrow(() -> new EmpruntNotFoundException(id));
    }

    public List<Emprunt> findActifs() {
        return empruntRepository.findEmpruntsActifs();
    }

    public List<Emprunt> findHistorique() {
        return empruntRepository.findHistorique();
    }

    public List<Emprunt> findByUtilisateur(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurService.findById(utilisateurId);
        return empruntRepository.findByUtilisateurOrderByDateEmpruntDesc(utilisateur);
    }

    public List<Emprunt> findByLivre(Long livreId) {
        Livre livre = livreService.findById(livreId);
        return empruntRepository.findByLivre(livre);
    }

    public List<Emprunt> findEnRetard() {
        LocalDate dateLimit = LocalDate.now().minusDays(30);
        return empruntRepository.findEmpruntsEnRetard(dateLimit);
    }

    @Transactional
    public Emprunt creer(Long utilisateurId, Long livreId) {
        Utilisateur utilisateur = utilisateurService.findById(utilisateurId);
        Livre livre = livreRepository.findByIdForUpdate(livreId)
                .orElseThrow(() -> new com.bibliotheque.exception.LivreNotFoundException(livreId));

        verifierDisponibiliteLivre(livre);

        Emprunt emprunt = new Emprunt(utilisateur, livre);
        livre.setDisponible(false);

        try {
            return empruntRepository.saveAndFlush(emprunt);
        } catch (DataIntegrityViolationException exception) {
            throw new LivreNonDisponibleException(livre.getTitre());
        }
    }

    @Transactional
    public Emprunt effectuerRetour(Long empruntId) {
        Emprunt emprunt = findById(empruntId);

        if (!emprunt.estEnCours()) {
            throw new EmpruntDejaRetourneException(empruntId);
        }

        emprunt.setDateRetour(LocalDate.now());

        Livre livre = emprunt.getLivre();
        boolean livreEncoreEmprunte = empruntRepository.existsByLivreAndDateRetourIsNull(livre);
        livre.setDisponible(!livreEncoreEmprunte);

        return empruntRepository.save(emprunt);
    }

    public long countActifs() {
        return empruntRepository.countByDateRetourIsNull();
    }

    public long countEnRetard() {
        LocalDate dateLimit = LocalDate.now().minusDays(30);
        return empruntRepository.countEmpruntsEnRetard(dateLimit);
    }

    public long countTotal() {
        return empruntRepository.count();
    }

    public boolean estEnCours(Long empruntId) {
        Emprunt emprunt = findById(empruntId);
        return emprunt.estEnCours();
    }

    private void verifierDisponibiliteLivre(Livre livre) {
        if (empruntRepository.existsByLivreAndDateRetourIsNull(livre)) {
            throw new LivreNonDisponibleException(livre.getTitre());
        }
    }
}
