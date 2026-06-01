package com.bibliotheque.service;

import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.activite.ActiveLoan;
import com.bibliotheque.model.activite.LoanActivity;
import com.bibliotheque.model.activite.LoanHistory;
import com.bibliotheque.model.activite.LoanStatus;
import com.bibliotheque.model.activite.filter.ActiveLoanFilter;
import com.bibliotheque.model.activite.filter.HistoryFilter;
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
    private static final String QUERY_VALUE_TOUS = "tous";

    private final EmpruntRepository empruntRepository;

    public LoanActivityService(EmpruntRepository empruntRepository) {
        this.empruntRepository = empruntRepository;
    }

    public LoanActivity getLoanActivity(String searchUser, String searchBook, int page,
                                        String statutActif, String statutHistorique) {
        boolean hasSearch = isNotBlank(searchUser) || isNotBlank(searchBook);
        ActiveLoanFilter activeFilter = ActiveLoanFilter.fromQueryParam(statutActif);
        HistoryFilter historyFilter = HistoryFilter.fromQueryParam(statutHistorique);

        List<Emprunt> activeEmprunts = fetchActiveLoans(searchUser, searchBook);
        long activeSectionTotal = activeEmprunts.size();

        List<ActiveLoan> activeLoans = activeEmprunts.stream()
                .filter(activeFilter::matches)
                .map(LoanActivityService::toActiveLoan)
                .toList();

        String normUser = normalizedSearch(searchUser);
        String normBook = normalizedSearch(searchBook);
        String historyStatut = historyFilter.queryValue();
        PageRequest pageRequest = PageRequest.of(page, DEFAULT_PAGE_SIZE);
        Page<Emprunt> historyPage = empruntRepository.findHistoryPaged(normUser, normBook, historyStatut, pageRequest);

        long historySectionTotal = empruntRepository.countHistoryFiltered(normUser, normBook, QUERY_VALUE_TOUS);
        long paginationTotal = empruntRepository.countHistoryFiltered(normUser, normBook, historyStatut);

        List<LoanHistory> history = historyPage.getContent().stream()
                .map(LoanActivityService::toLoanHistory)
                .toList();

        int safePage = historyPage.getNumber();
        int totalPages = historyPage.getTotalPages();

        return new LoanActivity(activeLoans, activeSectionTotal, history, safePage, totalPages,
                historySectionTotal, paginationTotal, hasSearch);
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
