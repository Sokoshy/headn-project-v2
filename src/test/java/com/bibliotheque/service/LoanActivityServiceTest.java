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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    // ---- existing tests (adapted) ----

    @Test
    void getLoanActivity_separatesActiveLoansAndHistory() {
        Livre livre1 = new Livre("Dune", "Frank Herbert");
        livre1.setId(1L);
        Livre livre2 = new Livre("1984", "George Orwell");
        livre2.setId(2L);
        Livre livre3 = new Livre("Fondation", "Isaac Asimov");
        livre3.setId(3L);

        Utilisateur alice = new Utilisateur("Alice", "alice@example.com");
        alice.setId(1L);
        Utilisateur bob = new Utilisateur("Bob", "bob@example.com");
        bob.setId(2L);

        Emprunt actif = new Emprunt(alice, livre1);
        actif.setId(10L);
        actif.setDateEmprunt(LocalDate.now().minusDays(5));

        Emprunt historique = new Emprunt(bob, livre2);
        historique.setId(20L);
        historique.setDateEmprunt(LocalDate.now().minusDays(60));
        historique.setDateRetour(LocalDate.now().minusDays(40));

        Emprunt enRetard = new Emprunt(alice, livre3);
        enRetard.setId(30L);
        enRetard.setDateEmprunt(LocalDate.now().minusDays(40));

        when(empruntRepository.findAllWithDetails())
                .thenReturn(List.of(actif, historique, enRetard));

        LoanActivity activity = loanActivityService.getLoanActivity();

        assertThat(activity.activeLoans()).hasSize(2);
        assertThat(activity.history()).hasSize(1);

        ActiveLoan premierActif = activity.activeLoans().get(0);
        assertThat(premierActif.empruntId()).isEqualTo(10L);
        assertThat(premierActif.bookTitle()).isEqualTo("Dune");
        assertThat(premierActif.userName()).isEqualTo("Alice");
        assertThat(premierActif.borrowDate()).isEqualTo(LocalDate.now().minusDays(5));
        assertThat(premierActif.status()).isEqualTo(LoanStatus.ACTIVE);

        ActiveLoan retarde = activity.activeLoans().get(1);
        assertThat(retarde.empruntId()).isEqualTo(30L);
        assertThat(retarde.status()).isEqualTo(LoanStatus.OVERDUE);

        LoanHistory hist = activity.history().get(0);
        assertThat(hist.empruntId()).isEqualTo(20L);
        assertThat(hist.bookTitle()).isEqualTo("1984");
        assertThat(hist.userName()).isEqualTo("Bob");
        assertThat(hist.returnDate()).isEqualTo(LocalDate.now().minusDays(40));

        assertThat(activity.page()).isEqualTo(0);
        assertThat(activity.totalPages()).isEqualTo(1);
        assertThat(activity.totalElements()).isEqualTo(1L);
        assertThat(activity.hasSearch()).isFalse();
    }

    @Test
    void getLoanActivity_handlesEmptyData() {
        when(empruntRepository.findAllWithDetails()).thenReturn(List.of());

        LoanActivity activity = loanActivityService.getLoanActivity();

        assertThat(activity.activeLoans()).isEmpty();
        assertThat(activity.history()).isEmpty();
        assertThat(activity.totalElements()).isEqualTo(0L);
        assertThat(activity.hasSearch()).isFalse();
    }

    // ---- new tests: filtering ----

    @Test
    void getLoanActivity_filtersHistoryByUserName() {
        Emprunt h1 = createHistoryEntry(1L, "Alice", "Dune", 60, 30);
        Emprunt h2 = createHistoryEntry(2L, "Bob", "1984", 50, 20);
        Emprunt h3 = createHistoryEntry(3L, "Alicia", "Fondation", 40, 10);

        when(empruntRepository.findAllWithDetails()).thenReturn(List.of(h1, h2, h3));

        LoanActivity activity = loanActivityService.getLoanActivity("Ali", null, 0);

        assertThat(activity.history()).hasSize(2);
        assertThat(activity.history()).extracting(LoanHistory::userName)
                .containsExactlyInAnyOrder("Alice", "Alicia");
        assertThat(activity.totalElements()).isEqualTo(2L);
        assertThat(activity.hasSearch()).isTrue();
    }

    @Test
    void getLoanActivity_filtersHistoryByBookTitle() {
        Emprunt h1 = createHistoryEntry(1L, "Alice", "Dune", 60, 30);
        Emprunt h2 = createHistoryEntry(2L, "Bob", "Fondation", 50, 20);

        when(empruntRepository.findAllWithDetails()).thenReturn(List.of(h1, h2));

        LoanActivity activity = loanActivityService.getLoanActivity(null, "dun", 0);

        assertThat(activity.history()).hasSize(1);
        assertThat(activity.history().get(0).bookTitle()).isEqualTo("Dune");
        assertThat(activity.totalElements()).isEqualTo(1L);
    }

    @Test
    void getLoanActivity_combinesUserAndBookFilters() {
        Emprunt h1 = createHistoryEntry(1L, "Alice", "Dune", 60, 30);
        Emprunt h2 = createHistoryEntry(2L, "Bob", "Dune", 50, 20);
        Emprunt h3 = createHistoryEntry(3L, "Alice", "Fondation", 40, 10);

        when(empruntRepository.findAllWithDetails()).thenReturn(List.of(h1, h2, h3));

        LoanActivity activity = loanActivityService.getLoanActivity("alice", "dune", 0);

        assertThat(activity.history()).hasSize(1);
        assertThat(activity.history().get(0).userName()).isEqualTo("Alice");
        assertThat(activity.history().get(0).bookTitle()).isEqualTo("Dune");
    }

    @Test
    void getLoanActivity_returnsEmptyHistoryWhenNoMatch() {
        Emprunt h1 = createHistoryEntry(1L, "Alice", "Dune", 60, 30);

        when(empruntRepository.findAllWithDetails()).thenReturn(List.of(h1));

        LoanActivity activity = loanActivityService.getLoanActivity("Zorro", null, 0);

        assertThat(activity.history()).isEmpty();
        assertThat(activity.totalElements()).isEqualTo(0L);
        assertThat(activity.hasSearch()).isTrue();
    }

    @Test
    void getLoanActivity_doesNotFilterActiveLoans() {
        Emprunt actif = createActiveEntry(1L, "Alice", "Dune", 5);

        when(empruntRepository.findAllWithDetails()).thenReturn(List.of(actif));

        LoanActivity activity = loanActivityService.getLoanActivity("Zorro", "Aucun", 0);

        assertThat(activity.activeLoans()).hasSize(1);
        assertThat(activity.history()).isEmpty();
    }

    @Test
    void getLoanActivity_ignoresBlankSearchTerms() {
        Emprunt h1 = createHistoryEntry(1L, "Alice", "Dune", 60, 30);
        Emprunt h2 = createHistoryEntry(2L, "Bob", "1984", 50, 20);

        when(empruntRepository.findAllWithDetails()).thenReturn(List.of(h1, h2));

        LoanActivity activity = loanActivityService.getLoanActivity("   ", null, 0);

        assertThat(activity.history()).hasSize(2);
        assertThat(activity.hasSearch()).isFalse();
    }

    // ---- new tests: pagination ----

    @Test
    void getLoanActivity_paginatesHistory() {
        List<Emprunt> historyEntries = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            historyEntries.add(createHistoryEntry(
                    (long) i, "User" + i, "Book" + i, 60 - i, 30 - i));
        }

        when(empruntRepository.findAllWithDetails()).thenReturn(historyEntries);

        LoanActivity page1 = loanActivityService.getLoanActivity(null, null, 0);

        assertThat(page1.history()).hasSize(10);
        assertThat(page1.page()).isEqualTo(0);
        assertThat(page1.totalPages()).isEqualTo(3);
        assertThat(page1.totalElements()).isEqualTo(25L);

        LoanActivity page3 = loanActivityService.getLoanActivity(null, null, 2);
        assertThat(page3.history()).hasSize(5);
        assertThat(page3.page()).isEqualTo(2);
    }

    @Test
    void getLoanActivity_clampsPageToValidRange() {
        List<Emprunt> historyEntries = List.of(
                createHistoryEntry(1L, "Alice", "Dune", 60, 30));

        when(empruntRepository.findAllWithDetails()).thenReturn(historyEntries);

        LoanActivity activity = loanActivityService.getLoanActivity(null, null, 99);

        assertThat(activity.page()).isEqualTo(0);
        assertThat(activity.history()).hasSize(1);
    }

    // ---- new tests: count methods ----

    @Test
    void countActiveLoans_delegatesToRepository() {
        when(empruntRepository.countByDateRetourIsNull()).thenReturn(5L);

        assertThat(loanActivityService.countActiveLoans()).isEqualTo(5L);
    }

    @Test
    void countOverdueLoans_delegatesToRepository() {
        when(empruntRepository.countEmpruntsEnRetard(LocalDate.now().minusDays(30))).thenReturn(2L);

        assertThat(loanActivityService.countOverdueLoans()).isEqualTo(2L);
    }

    // ---- helpers ----

    private Emprunt createHistoryEntry(Long id, String userName, String bookTitle,
                                       int daysSinceBorrow, int daysSinceReturn) {
        Livre livre = new Livre(bookTitle, "Author");
        livre.setId(id);
        Utilisateur utilisateur = new Utilisateur(userName, userName.toLowerCase() + "@test.com");
        utilisateur.setId(id);

        Emprunt e = new Emprunt(utilisateur, livre);
        e.setId(id);
        e.setDateEmprunt(LocalDate.now().minusDays(daysSinceBorrow));
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
        return e;
    }
}