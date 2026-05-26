package com.bibliotheque.service;

import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.activite.ActiveLoan;
import com.bibliotheque.model.activite.LoanActivity;
import com.bibliotheque.model.activite.LoanHistory;
import com.bibliotheque.model.activite.LoanStatus;
import com.bibliotheque.repository.EmpruntRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class LoanActivityService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final EmpruntRepository empruntRepository;

    public LoanActivityService(EmpruntRepository empruntRepository) {
        this.empruntRepository = empruntRepository;
    }

    public LoanActivity getLoanActivity() {
        return getLoanActivity(null, null, 0, null);
    }

    public LoanActivity getLoanActivity(String searchUser, String searchBook, int page) {
        return getLoanActivity(searchUser, searchBook, page, null);
    }

    public LoanActivity getLoanActivity(String searchUser, String searchBook, int page, String statut) {
        List<Emprunt> all = empruntRepository.findAllWithDetails();

        List<ActiveLoan> activeLoans = new ArrayList<>();
        List<LoanHistory> history = new ArrayList<>();

        String effectiveStatut = (statut == null || statut.isBlank()) ? "tous" : statut;

        for (Emprunt e : all) {
            if (e.estEnCours()) {
                activeLoans.add(toActiveLoan(e));
            } else {
                history.add(toLoanHistory(e));
            }
        }

        // Apply status filter
        switch (effectiveStatut) {
            case "actifs" -> {
                activeLoans.removeIf(a -> a.status() == LoanStatus.OVERDUE);
                history.clear();
            }
            case "en_retard" -> {
                activeLoans.removeIf(a -> a.status() != LoanStatus.OVERDUE);
                history.clear();
            }
            case "termines" -> {
                activeLoans.clear();
                history.removeIf(h -> h.status() != LoanStatus.RETURNED);
            }
            case "rendus_en_retard" -> {
                activeLoans.clear();
                history.removeIf(h -> h.status() != LoanStatus.LATE_RETURN);
            }
            // "tous" — no filter
        }

        boolean hasSearch = isNotBlank(searchUser) || isNotBlank(searchBook);

        if (hasSearch) {
            history = filterHistory(history, searchUser, searchBook);
        }

        int totalFiltered = history.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalFiltered / DEFAULT_PAGE_SIZE));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));
        int fromIndex = safePage * DEFAULT_PAGE_SIZE;
        int toIndex = Math.min(fromIndex + DEFAULT_PAGE_SIZE, totalFiltered);

        List<LoanHistory> pagedHistory = history.subList(fromIndex, toIndex);

        return new LoanActivity(activeLoans, pagedHistory, safePage, totalPages, totalFiltered, hasSearch);
    }

    public long countActiveLoans() {
        return empruntRepository.countByDateRetourIsNull();
    }

    public long countOverdueLoans() {
        return empruntRepository.countByDateRetourIsNullAndDateRetourPrevueBefore(java.time.LocalDate.now());
    }

    private List<LoanHistory> filterHistory(List<LoanHistory> history, String searchUser, String searchBook) {
        String userLower = isNotBlank(searchUser) ? searchUser.toLowerCase().trim() : null;
        String bookLower = isNotBlank(searchBook) ? searchBook.toLowerCase().trim() : null;

        return history.stream()
                .filter(h -> {
                    boolean userMatch = userLower == null || h.userName().toLowerCase().contains(userLower);
                    boolean bookMatch = bookLower == null || h.bookTitle().toLowerCase().contains(bookLower);
                    return userMatch && bookMatch;
                })
                .toList();
    }

    private static ActiveLoan toActiveLoan(Emprunt e) {
        return new ActiveLoan(
                e.getId(),
                e.getLivre().getId(),
                e.getLivre().getTitre(),
                e.getUtilisateur().getId(),
                e.getUtilisateur().getNom(),
                e.getDateEmprunt(),
                e.getDateRetourPrevue(),
                e.estEnRetard() ? LoanStatus.OVERDUE : LoanStatus.ACTIVE
        );
    }

    private static LoanHistory toLoanHistory(Emprunt e) {
        return new LoanHistory(
                e.getId(),
                e.getLivre().getId(),
                e.getLivre().getTitre(),
                e.getUtilisateur().getId(),
                e.getUtilisateur().getNom(),
                e.getDateEmprunt(),
                e.getDateRetour(),
                e.getDateRetourPrevue(),
                e.estRenduEnRetard() ? LoanStatus.LATE_RETURN : LoanStatus.RETURNED
        );
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}