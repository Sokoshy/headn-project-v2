package com.bibliotheque.service;

import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.activite.ActiveLoan;
import com.bibliotheque.model.activite.LoanActivity;
import com.bibliotheque.model.activite.LoanHistory;
import com.bibliotheque.model.activite.LoanStatus;
import com.bibliotheque.repository.EmpruntRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
        boolean hasSearch = isNotBlank(searchUser) || isNotBlank(searchBook);
        String effectiveStatut = (statut == null || statut.isBlank()) ? "tous" : statut;

        // Fetch active loans (no pagination)
        List<Emprunt> activeEmprunts = fetchActiveLoans(searchUser, searchBook);

        // Fetch history with pagination
        String historyStatut = toHistoryStatut(effectiveStatut);
        PageRequest pageRequest = PageRequest.of(page, DEFAULT_PAGE_SIZE);
        Page<Emprunt> historyPage = empruntRepository.findHistoryPaged(
                normalizedSearch(searchUser), normalizedSearch(searchBook), historyStatut, pageRequest);
        long totalHistory = empruntRepository.countHistoryFiltered(
                normalizedSearch(searchUser), normalizedSearch(searchBook), historyStatut);

        // Map active loans (filtered by status if needed)
        List<ActiveLoan> activeLoans = mapActiveLoans(activeEmprunts, effectiveStatut);

        // Map history
        List<LoanHistory> history = historyPage.getContent().stream()
                .map(LoanActivityService::toLoanHistory)
                .toList();

        int safePage = historyPage.getNumber();
        int totalPages = historyPage.getTotalPages();

        return new LoanActivity(activeLoans, history, safePage, totalPages, totalHistory, hasSearch);
    }

    public long countActiveLoans() {
        return empruntRepository.countByDateRetourIsNull();
    }

    public long countOverdueLoans() {
        return empruntRepository.countByDateRetourIsNullAndDateRetourPrevueBefore(LocalDate.now());
    }

    private List<Emprunt> fetchActiveLoans(String searchUser, String searchBook) {
        if (isNotBlank(searchUser) || isNotBlank(searchBook)) {
            return empruntRepository.findActiveLoansFiltered(
                    normalizedSearch(searchUser), normalizedSearch(searchBook));
        }
        return empruntRepository.findActiveLoans();
    }

    private String toHistoryStatut(String statut) {
        return switch (statut) {
            case "termines", "rendus_en_retard" -> statut;
            default -> null; // "tous", "actifs", "en_retard" → no history filter
        };
    }

    private List<ActiveLoan> mapActiveLoans(List<Emprunt> emprunts, String statut) {
        List<ActiveLoan> all = emprunts.stream()
                .map(LoanActivityService::toActiveLoan)
                .toList();

        return switch (statut) {
            case "actifs" -> all.stream()
                    .filter(a -> a.status() == LoanStatus.ACTIVE)
                    .toList();
            case "en_retard" -> all.stream()
                    .filter(a -> a.status() == LoanStatus.OVERDUE)
                    .toList();
            case "termines", "rendus_en_retard" -> List.of(); // History-only filters
            default -> all; // "tous" — show all active
        };
    }

    private static String normalizedSearch(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return "%" + value.trim().toLowerCase() + "%";
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
