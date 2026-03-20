package com.bibliotheque.service;

import com.bibliotheque.exception.BusinessException;
import com.bibliotheque.exception.DuplicateResourceException;
import com.bibliotheque.exception.LivreNotFoundException;
import com.bibliotheque.model.Livre;
import com.bibliotheque.repository.EmpruntRepository;
import com.bibliotheque.repository.LivreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LivreService {

    private final LivreRepository livreRepository;
    private final EmpruntRepository empruntRepository;

    public LivreService(LivreRepository livreRepository, EmpruntRepository empruntRepository) {
        this.livreRepository = livreRepository;
        this.empruntRepository = empruntRepository;
    }

    public List<Livre> findAll() {
        return livreRepository.findAll();
    }

    public Livre findById(Long id) {
        return livreRepository.findById(id)
                .orElseThrow(() -> new LivreNotFoundException(id));
    }

    public List<Livre> findByRecherche(String recherche) {
        if (recherche == null || recherche.isBlank()) {
            return findAll();
        }
        return livreRepository.findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCase(
                recherche.trim(), recherche.trim());
    }

    public List<Livre> findDisponibles() {
        return livreRepository.findDisponibles();
    }

    @Transactional
    public Livre creer(Livre livre) {
        normaliserLivre(livre);
        validerUniciteTitreAuteur(livre.getTitre(), livre.getAuteur(), null);
        livre.setDisponible(true);
        return livreRepository.save(livre);
    }

    @Transactional
    public Livre modifier(Long id, Livre livreModifie) {
        Livre livreExistant = findById(id);
        normaliserLivre(livreModifie);
        validerUniciteTitreAuteur(livreModifie.getTitre(), livreModifie.getAuteur(), id);

        livreExistant.setTitre(livreModifie.getTitre());
        livreExistant.setAuteur(livreModifie.getAuteur());

        return livreRepository.save(livreExistant);
    }

    @Transactional
    public void supprimer(Long id) {
        Livre livre = findById(id);
        if (empruntRepository.existsByLivre(livre)) {
            throw new BusinessException(
                    String.format("Impossible de supprimer le livre '%s' : un historique d'emprunts lui est associé", livre.getTitre()),
                    "SUPPRESSION_IMPOSSIBLE");
        }
        livreRepository.delete(livre);
    }

    public boolean estDisponible(Long id) {
        findById(id);
        return !empruntRepository.existsByLivreIdAndDateRetourIsNull(id);
    }

    @Transactional
    public void marquerIndisponible(Long id) {
        Livre livre = findById(id);
        livre.setDisponible(false);
        livreRepository.save(livre);
    }

    @Transactional
    public void marquerDisponible(Long id) {
        Livre livre = findById(id);
        livre.setDisponible(true);
        livreRepository.save(livre);
    }

    public long countDisponibles() {
        return livreRepository.countDisponibles();
    }

    public long countTotal() {
        return livreRepository.count();
    }

    private void normaliserLivre(Livre livre) {
        if (livre.getTitre() != null) {
            livre.setTitre(livre.getTitre().trim());
        }
        if (livre.getAuteur() != null) {
            livre.setAuteur(livre.getAuteur().trim());
        }
    }

    private void validerUniciteTitreAuteur(String titre, String auteur, Long idExclu) {
        if (titre == null || auteur == null) {
            return;
        }
        String titreNormalise = titre.trim();
        String auteurNormalise = auteur.trim();

        List<Livre> livresCorrespondants = livreRepository.findByTitreContainingIgnoreCase(titreNormalise);
        boolean existe = livresCorrespondants.stream()
                .filter(l -> l.getTitre().equalsIgnoreCase(titreNormalise))
                .anyMatch(l -> l.getAuteur().equalsIgnoreCase(auteurNormalise)
                        && !l.getId().equals(idExclu));

        if (existe) {
            throw new DuplicateResourceException(
                    String.format("Un livre avec le titre '%s' de l'auteur '%s' existe déjà", titreNormalise, auteurNormalise));
        }
    }
}
