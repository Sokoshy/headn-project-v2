package com.bibliotheque.service;

import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.model.activite.ActiveLoan;
import com.bibliotheque.model.activite.LoanActivity;
import com.bibliotheque.model.activite.LoanHistory;
import com.bibliotheque.model.activite.LoanStatus;
import com.bibliotheque.repository.EmpruntRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanActivityServiceTest {

    @Mock
    private EmpruntRepository empruntRepository;

    private LoanActivityService loanActivityService;

    @BeforeEach
    void setUp() {
        loanActivityService = new LoanActivityService(empruntRepository);
    }

    // ---- Active loans ----

    @Test
    @DisplayName("getLoanActivity returns active loans from repository")
    void getLoanActivity_returnsActiveLoans() {
        Emprunt actif = createActiveEntry(1L, "Alice", "Dune", 5);

        when(empruntRepository.findActiveLoans()).thenReturn(List.of(actif));
        when(empruntRepository.findHistoryPaged(isNull(), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(empruntRepository.countHistoryFiltered(isNull(), isNull(), isNull())).thenReturn(0L);

        LoanActivity activity = loanActivityService.getLoanActivity();

        assertThat(activity.activeLoans()).hasSize(1);
        assertThat(activity.activeLoans().get(0).bookTitle()).isEqualTo("Dune");
        assertThat(activity.activeLoans().get(0).userName()).isEqualTo("Alice");
        assertThat(activity.activeLoans().get(0).status()).isEqualTo(LoanStatus.ACTIVE);
    }

    @Test
    @DisplayName("getLoanActivity includes overdue active loans")
    void getLoanActivity_includesOverdueActiveLoans() {
        Emprunt actif = createActiveEntry(1L, "Alice", "Dune", 5);
        Emprunt enRetard = createActiveEntry(2L, "Bob", "1984", 40);
        enRetard.setDateRetourPrevue(LocalDate.now().minusDays(10));

        when(empruntRepository.findActiveLoans()).thenReturn(List.of(actif, enRetard));
        when(empruntRepository.findHistoryPaged(isNull(), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(empruntRepository.countHistoryFiltered(isNull(), isNull(), isNull())).thenReturn(0L);

        LoanActivity activity = loanActivityService.getLoanActivity();

        assertThat(activity.activeLoans()).hasSize(2);
        assertThat(activity.activeLoans().get(0).status()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(activity.activeLoans().get(1).status()).isEqualTo(LoanStatus.OVERDUE);
    }

    @Test
    @DisplayName("getLoanActivity returns history from repository with pagination")
    void getLoanActivity_returnsHistoryPaged() {
        Emprunt hist = createHistoryEntry(1L, "Bob", "1984", 60, 30);

        when(empruntRepository.findActiveLoans()).thenReturn(List.of());
        when(empruntRepository.findHistoryPaged(isNull(), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(hist)));
        when(empruntRepository.countHistoryFiltered(isNull(), isNull(), isNull())).thenReturn(1L);

        LoanActivity activity = loanActivityService.getLoanActivity();

        assertThat(activity.history()).hasSize(1);
        assertThat(activity.history().get(0).bookTitle()).isEqualTo("1984");
        assertThat(activity.history().get(0).status()).isEqualTo(LoanStatus.RETURNED);
        assertThat(activity.totalElements()).isEqualTo(1L);
        assertThat(activity.hasSearch()).isFalse();
    }

    @Test
    @DisplayName("getLoanActivity handles empty data")
    void getLoanActivity_handlesEmptyData() {
        when(empruntRepository.findActiveLoans()).thenReturn(List.of());
        when(empruntRepository.findHistoryPaged(isNull(), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(empruntRepository.countHistoryFiltered(isNull(), isNull(), isNull())).thenReturn(0L);

        LoanActivity activity = loanActivityService.getLoanActivity();

        assertThat(activity.activeLoans()).isEmpty();
        assertThat(activity.history()).isEmpty();
        assertThat(activity.totalElements()).isEqualTo(0L);
        assertThat(activity.hasSearch()).isFalse();
    }

    // ---- Search filtering ----

    @Test
    @DisplayName("getLoanActivity passes search filters to repository for active loans")
    void getLoanActivity_filtersActiveLoansBySearch() {
        Emprunt actif = createActiveEntry(1L, "Alice", "Dune", 5);

        when(empruntRepository.findActiveLoansFiltered("%ali%", null)).thenReturn(List.of(actif));
        when(empruntRepository.findHistoryPaged(eq("%ali%"), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(empruntRepository.countHistoryFiltered(eq("%ali%"), isNull(), isNull())).thenReturn(0L);

        LoanActivity activity = loanActivityService.getLoanActivity("Ali", null, 0);

        assertThat(activity.activeLoans()).hasSize(1);
        assertThat(activity.hasSearch()).isTrue();
    }

    @Test
    @DisplayName("getLoanActivity passes search filters to repository for history")
    void getLoanActivity_filtersHistoryBySearch() {
        Emprunt hist = createHistoryEntry(1L, "Alice", "Dune", 60, 30);

        when(empruntRepository.findActiveLoansFiltered("%ali%", null)).thenReturn(List.of());
        when(empruntRepository.findHistoryPaged(eq("%ali%"), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(hist)));
        when(empruntRepository.countHistoryFiltered(eq("%ali%"), isNull(), isNull())).thenReturn(1L);

        LoanActivity activity = loanActivityService.getLoanActivity("Ali", null, 0);

        assertThat(activity.history()).hasSize(1);
        assertThat(activity.history().get(0).userName()).isEqualTo("Alice");
        assertThat(activity.totalElements()).isEqualTo(1L);
        assertThat(activity.hasSearch()).isTrue();
    }

    @Test
    @DisplayName("getLoanActivity ignores blank search terms")
    void getLoanActivity_ignoresBlankSearchTerms() {
        Emprunt actif = createActiveEntry(1L, "Alice", "Dune", 5);

        when(empruntRepository.findActiveLoans()).thenReturn(List.of(actif));
        when(empruntRepository.findHistoryPaged(isNull(), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(empruntRepository.countHistoryFiltered(isNull(), isNull(), isNull())).thenReturn(0L);

        LoanActivity activity = loanActivityService.getLoanActivity("   ", null, 0);

        assertThat(activity.activeLoans()).hasSize(1);
        assertThat(activity.hasSearch()).isFalse();
    }

    // ---- Status filtering ----

    @Test
    @DisplayName("filter actifs shows only non-overdue active loans")
    void getLoanActivity_filterActifs_showsOnlyActiveWithoutOverdue() {
        Emprunt actif = createActiveEntry(1L, "Alice", "Dune", 5);
        actif.setDateRetourPrevue(LocalDate.now().plusDays(25));
        Emprunt enRetard = createActiveEntry(2L, "Bob", "1984", 40);
        enRetard.setDateRetourPrevue(LocalDate.now().minusDays(10));

        when(empruntRepository.findActiveLoans()).thenReturn(List.of(actif, enRetard));
        when(empruntRepository.findHistoryPaged(isNull(), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(empruntRepository.countHistoryFiltered(isNull(), isNull(), isNull())).thenReturn(0L);

        LoanActivity activity = loanActivityService.getLoanActivity(null, null, 0, "actifs");

        assertThat(activity.activeLoans()).hasSize(1);
        assertThat(activity.activeLoans().get(0).status()).isEqualTo(LoanStatus.ACTIVE);
    }

    @Test
    @DisplayName("filter en_retard shows only overdue active loans")
    void getLoanActivity_filterEnRetard_showsOnlyOverdue() {
        Emprunt actif = createActiveEntry(1L, "Alice", "Dune", 5);
        actif.setDateRetourPrevue(LocalDate.now().plusDays(25));
        Emprunt enRetard = createActiveEntry(2L, "Bob", "1984", 40);
        enRetard.setDateRetourPrevue(LocalDate.now().minusDays(10));

        when(empruntRepository.findActiveLoans()).thenReturn(List.of(actif, enRetard));
        when(empruntRepository.findHistoryPaged(isNull(), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(empruntRepository.countHistoryFiltered(isNull(), isNull(), isNull())).thenReturn(0L);

        LoanActivity activity = loanActivityService.getLoanActivity(null, null, 0, "en_retard");

        assertThat(activity.activeLoans()).hasSize(1);
        assertThat(activity.activeLoans().get(0).status()).isEqualTo(LoanStatus.OVERDUE);
    }

    @Test
    @DisplayName("filter termines shows only returned-on-time history")
    void getLoanActivity_filterTermines_showsOnlyReturnedLoans() {
        Emprunt rendu = createHistoryEntry(2L, "Bob", "1984", 60, 5);
        rendu.setDateRetourPrevue(LocalDate.now().plusDays(10));

        when(empruntRepository.findActiveLoans()).thenReturn(List.of());
        when(empruntRepository.findHistoryPaged(isNull(), isNull(), eq("termines"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(rendu)));
        when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq("termines"))).thenReturn(1L);

        LoanActivity activity = loanActivityService.getLoanActivity(null, null, 0, "termines");

        assertThat(activity.activeLoans()).isEmpty();
        assertThat(activity.history()).hasSize(1);
        assertThat(activity.history().get(0).status()).isEqualTo(LoanStatus.RETURNED);
    }

    @Test
    @DisplayName("filter termines hides active loans even when repository returns them")
    void getLoanActivity_filterTermines_hidesActiveLoans() {
        // Active loans exist in the repository
        Emprunt actif = createActiveEntry(1L, "Alice", "Dune", 5);
        Emprunt enRetard = createActiveEntry(2L, "Bob", "1984", 40);
        enRetard.setDateRetourPrevue(LocalDate.now().minusDays(10));

        when(empruntRepository.findActiveLoans()).thenReturn(List.of(actif, enRetard));
        when(empruntRepository.findHistoryPaged(isNull(), isNull(), eq("termines"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq("termines"))).thenReturn(0L);

        LoanActivity activity = loanActivityService.getLoanActivity(null, null, 0, "termines");

        // Active loans must be hidden for "termines" filter
        assertThat(activity.activeLoans()).isEmpty();
    }

    @Test
    @DisplayName("filter rendus_en_retard shows only late-returned history")
    void getLoanActivity_filterRendusEnRetard_showsOnlyLateReturned() {
        Emprunt renduEnRetard = createHistoryEntry(3L, "Charlie", "Fondation", 60, 30);
        renduEnRetard.setDateRetourPrevue(LocalDate.now().minusDays(50));

        when(empruntRepository.findActiveLoans()).thenReturn(List.of());
        when(empruntRepository.findHistoryPaged(isNull(), isNull(), eq("rendus_en_retard"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(renduEnRetard)));
        when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq("rendus_en_retard"))).thenReturn(1L);

        LoanActivity activity = loanActivityService.getLoanActivity(null, null, 0, "rendus_en_retard");

        assertThat(activity.activeLoans()).isEmpty();
        assertThat(activity.history()).hasSize(1);
        assertThat(activity.history().get(0).status()).isEqualTo(LoanStatus.LATE_RETURN);
    }

    @Test
    @DisplayName("filter rendus_en_retard hides active loans even when repository returns them")
    void getLoanActivity_filterRendusEnRetard_hidesActiveLoans() {
        // Active loans exist in the repository
        Emprunt actif = createActiveEntry(1L, "Alice", "Dune", 5);
        Emprunt enRetard = createActiveEntry(2L, "Bob", "1984", 40);
        enRetard.setDateRetourPrevue(LocalDate.now().minusDays(10));

        when(empruntRepository.findActiveLoans()).thenReturn(List.of(actif, enRetard));
        when(empruntRepository.findHistoryPaged(isNull(), isNull(), eq("rendus_en_retard"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq("rendus_en_retard"))).thenReturn(0L);

        LoanActivity activity = loanActivityService.getLoanActivity(null, null, 0, "rendus_en_retard");

        // Active loans must be hidden for "rendus_en_retard" filter
        assertThat(activity.activeLoans()).isEmpty();
    }

    @Test
    @DisplayName("filter tous shows both active loans and history")
    void getLoanActivity_filterTous_showsActiveAndHistory() {
        Emprunt actif = createActiveEntry(1L, "Alice", "Dune", 5);
        Emprunt hist = createHistoryEntry(2L, "Bob", "1984", 60, 30);

        when(empruntRepository.findActiveLoans()).thenReturn(List.of(actif));
        when(empruntRepository.findHistoryPaged(isNull(), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(hist)));
        when(empruntRepository.countHistoryFiltered(isNull(), isNull(), isNull())).thenReturn(1L);

        LoanActivity activity = loanActivityService.getLoanActivity(null, null, 0, "tous");

        assertThat(activity.activeLoans()).hasSize(1);
        assertThat(activity.history()).hasSize(1);
    }

    // ---- Pagination ----

    @Test
    @DisplayName("pagination is handled by repository")
    void getLoanActivity_paginationHandledByRepository() {
        PageRequest pageRequest = PageRequest.of(2, 10);
        Emprunt hist = createHistoryEntry(1L, "Alice", "Dune", 60, 30);

        when(empruntRepository.findActiveLoans()).thenReturn(List.of());
        when(empruntRepository.findHistoryPaged(isNull(), isNull(), isNull(), eq(pageRequest)))
                .thenReturn(new PageImpl<>(List.of(hist), pageRequest, 25));
        when(empruntRepository.countHistoryFiltered(isNull(), isNull(), isNull())).thenReturn(25L);

        LoanActivity activity = loanActivityService.getLoanActivity(null, null, 2);

        assertThat(activity.history()).hasSize(1);
        assertThat(activity.page()).isEqualTo(2);
        assertThat(activity.totalPages()).isEqualTo(3);
        assertThat(activity.totalElements()).isEqualTo(25L);
    }

    // ---- Count methods ----

    @Test
    @DisplayName("countActiveLoans delegates to repository")
    void countActiveLoans_delegatesToRepository() {
        when(empruntRepository.countByDateRetourIsNull()).thenReturn(5L);

        assertThat(loanActivityService.countActiveLoans()).isEqualTo(5L);
    }

    @Test
    @DisplayName("countOverdueLoans delegates to repository")
    void countOverdueLoans_delegatesToRepository() {
        when(empruntRepository.countByDateRetourIsNullAndDateRetourPrevueBefore(LocalDate.now()))
                .thenReturn(3L);

        assertThat(loanActivityService.countOverdueLoans()).isEqualTo(3L);
    }

    // ---- Helpers ----

    private Emprunt createHistoryEntry(Long id, String userName, String bookTitle,
                                       int daysSinceBorrow, int daysSinceReturn) {
        Livre livre = new Livre(bookTitle, "Author");
        livre.setId(id);
        Utilisateur utilisateur = new Utilisateur(userName, userName.toLowerCase() + "@test.com");
        utilisateur.setId(id);

        Emprunt e = new Emprunt(utilisateur, livre);
        e.setId(id);
        e.setDateEmprunt(LocalDate.now().minusDays(daysSinceBorrow));
        e.setDateRetourPrevue(LocalDate.now().minusDays(daysSinceBorrow).plusDays(30));
        e.setDateRetour(LocalDate.now().minusDays(daysSinceReturn));
        return e;
    }

    private Emprunt createActiveEntry(Long id, String userName, String bookTitle,
                                      int daysSinceBorrow) {
        Livre livre = new Livre(bookTitle, "Author");
        livre.setId(id);
        Utilisateur utilisateur = new Utilisateur(userName, userName.toLowerCase() + "@test.com");
        utilisateur.setId(id);

        Emprunt e = new Emprunt(utilisateur, livre);
        e.setId(id);
        e.setDateEmprunt(LocalDate.now().minusDays(daysSinceBorrow));
        e.setDateRetourPrevue(LocalDate.now().plusDays(30));
        return e;
    }
}
