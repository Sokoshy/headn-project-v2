package com.bibliotheque.service;

import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.model.preparation.LoanPreparation;
import com.bibliotheque.model.preparation.SelectableBook;
import com.bibliotheque.model.preparation.SelectableUser;
import com.bibliotheque.repository.LivreRepository;
import com.bibliotheque.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class LoanPreparationService {

    private static final long DEFAULT_LOAN_DURATION_DAYS = 30;

    private final LivreRepository livreRepository;
    private final UtilisateurRepository utilisateurRepository;

    public LoanPreparationService(LivreRepository livreRepository,
                                  UtilisateurRepository utilisateurRepository) {
        this.livreRepository = livreRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public LoanPreparation getPreparation() {
        List<SelectableBook> books = livreRepository.findDisponibles().stream()
                .map(LoanPreparationService::toSelectableBook)
                .toList();

        List<SelectableUser> users = utilisateurRepository.findAll().stream()
                .map(LoanPreparationService::toSelectableUser)
                .toList();

        LocalDate defaultExpectedReturnDate = LocalDate.now().plusDays(DEFAULT_LOAN_DURATION_DAYS);

        return new LoanPreparation(books, users, defaultExpectedReturnDate);
    }

    private static SelectableBook toSelectableBook(Livre livre) {
        return new SelectableBook(livre.getId(), livre.getTitre(), livre.getAuteur());
    }

    private static SelectableUser toSelectableUser(Utilisateur utilisateur) {
        return new SelectableUser(utilisateur.getId(), utilisateur.getNom());
    }
}
