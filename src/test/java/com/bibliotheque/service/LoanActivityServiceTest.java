package com.bibliotheque.service;

import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.model.activite.LoanActivity;
import com.bibliotheque.model.activite.LoanStatus;
import com.bibliotheque.repository.EmpruntRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanActivityServiceTest {

    private static final String QUERY_VALUE_TOUS = "tous";
    private static final String QUERY_VALUE_ACTIFS = "actifs";
    private static final String QUERY_VALUE_EN_RETARD = "en_retard";
    private static final String QUERY_VALUE_TERMINES = "termines";
    private static final String QUERY_VALUE_RENDUS_EN_RETARD = "rendus_en_retard";

    @Mock
    private EmpruntRepository empruntRepository;

    private LoanActivityService loanActivityService;

    @BeforeEach
    void setUp() {
        loanActivityService = new LoanActivityService(empruntRepository);
    }

    @Nested
    @DisplayName("Section active : liste et compteur")
    class ActiveSection {

        @Test
        @DisplayName("sans recherche et filtres par défaut, retourne les emprunts actifs du repository")
        void returnsActiveLoansFromRepository() {
            Emprunt actif = createActiveEntry(1L, "Alice", "Dune", 5);

            when(empruntRepository.findActiveLoans()).thenReturn(List.of(actif));
            when(empruntRepository.findHistoryPaged(isNull(), isNull(), eq(QUERY_VALUE_TOUS), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of()));
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_TOUS))).thenReturn(0L);

            LoanActivity activity = loanActivityService.getLoanActivity(null, null, 0, null, null);

            assertThat(activity.activeLoans()).hasSize(1);
            assertThat(activity.activeLoans().get(0).bookTitle()).isEqualTo("Dune");
            assertThat(activity.activeLoans().get(0).userName()).isEqualTo("Alice");
            assertThat(activity.activeLoans().get(0).status()).isEqualTo(LoanStatus.ACTIVE);
        }

        @Test
        @DisplayName("inclut les emprunts en retard dans la liste active par défaut")
        void includesOverdueActiveLoansByDefault() {
            Emprunt actif = createActiveEntry(1L, "Alice", "Dune", 5);
            Emprunt enRetard = createActiveEntry(2L, "Bob", "1984", 40);
            enRetard.setDateRetourPrevue(LocalDate.now().minusDays(10));

            when(empruntRepository.findActiveLoans()).thenReturn(List.of(actif, enRetard));
            when(empruntRepository.findHistoryPaged(isNull(), isNull(), eq(QUERY_VALUE_TOUS), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of()));
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_TOUS))).thenReturn(0L);

            LoanActivity activity = loanActivityService.getLoanActivity(null, null, 0, null, null);

            assertThat(activity.activeLoans()).hasSize(2);
            assertThat(activity.activeLoans().get(0).status()).isEqualTo(LoanStatus.ACTIVE);
            assertThat(activity.activeLoans().get(1).status()).isEqualTo(LoanStatus.OVERDUE);
        }

        @Test
        @DisplayName("filtre actifs : ne retourne que les emprunts actifs non en retard")
        void filterActifs_keepsOnlyNonOverdue() {
            Emprunt actif = createActiveEntry(1L, "Alice", "Dune", 5);
            actif.setDateRetourPrevue(LocalDate.now().plusDays(25));
            Emprunt enRetard = createActiveEntry(2L, "Bob", "1984", 40);
            enRetard.setDateRetourPrevue(LocalDate.now().minusDays(10));

            when(empruntRepository.findActiveLoans()).thenReturn(List.of(actif, enRetard));
            when(empruntRepository.findHistoryPaged(isNull(), isNull(), eq(QUERY_VALUE_TOUS), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of()));
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_TOUS))).thenReturn(0L);

            LoanActivity activity = loanActivityService.getLoanActivity(null, null, 0, QUERY_VALUE_ACTIFS, null);

            assertThat(activity.activeLoans()).hasSize(1);
            assertThat(activity.activeLoans().get(0).status()).isEqualTo(LoanStatus.ACTIVE);
        }

        @Test
        @DisplayName("filtre en_retard : ne retourne que les emprunts en retard")
        void filterEnRetard_keepsOnlyOverdue() {
            Emprunt actif = createActiveEntry(1L, "Alice", "Dune", 5);
            actif.setDateRetourPrevue(LocalDate.now().plusDays(25));
            Emprunt enRetard = createActiveEntry(2L, "Bob", "1984", 40);
            enRetard.setDateRetourPrevue(LocalDate.now().minusDays(10));

            when(empruntRepository.findActiveLoans()).thenReturn(List.of(actif, enRetard));
            when(empruntRepository.findHistoryPaged(isNull(), isNull(), eq(QUERY_VALUE_TOUS), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of()));
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_TOUS))).thenReturn(0L);

            LoanActivity activity = loanActivityService.getLoanActivity(null, null, 0, QUERY_VALUE_EN_RETARD, null);

            assertThat(activity.activeLoans()).hasSize(1);
            assertThat(activity.activeLoans().get(0).status()).isEqualTo(LoanStatus.OVERDUE);
        }

        @Test
        @DisplayName("filtre d'historique n'affecte pas la liste active")
        void historyFilter_doesNotAffectActiveList() {
            Emprunt actif = createActiveEntry(1L, "Alice", "Dune", 5);
            Emprunt enRetard = createActiveEntry(2L, "Bob", "1984", 40);
            enRetard.setDateRetourPrevue(LocalDate.now().minusDays(10));

            when(empruntRepository.findActiveLoans()).thenReturn(List.of(actif, enRetard));
            when(empruntRepository.findHistoryPaged(isNull(), isNull(), eq(QUERY_VALUE_TERMINES), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of()));
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_TOUS))).thenReturn(0L);
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_TERMINES))).thenReturn(0L);

            LoanActivity activity = loanActivityService.getLoanActivity(null, null, 0, null, QUERY_VALUE_TERMINES);

            assertThat(activity.activeLoans()).hasSize(2);
        }

        @Test
        @DisplayName("compteur de section active = taille de la liste chargée, indépendant du filtre actif")
        void activeSectionTotal_independentOfActiveFilter() {
            Emprunt actif = createActiveEntry(1L, "Alice", "Dune", 5);
            actif.setDateRetourPrevue(LocalDate.now().plusDays(25));
            Emprunt enRetard = createActiveEntry(2L, "Bob", "1984", 40);
            enRetard.setDateRetourPrevue(LocalDate.now().minusDays(10));

            when(empruntRepository.findActiveLoans()).thenReturn(List.of(actif, enRetard));
            when(empruntRepository.findHistoryPaged(isNull(), isNull(), eq(QUERY_VALUE_TOUS), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of()));
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_TOUS))).thenReturn(0L);

            LoanActivity avecFiltre = loanActivityService.getLoanActivity(null, null, 0, QUERY_VALUE_EN_RETARD, null);
            assertThat(avecFiltre.activeLoans()).hasSize(1);
            assertThat(avecFiltre.activeTotalElements()).isEqualTo(2L);

            LoanActivity sansFiltre = loanActivityService.getLoanActivity(null, null, 0, QUERY_VALUE_TOUS, null);
            assertThat(sansFiltre.activeLoans()).hasSize(2);
            assertThat(sansFiltre.activeTotalElements()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("Section historique : liste, filtre, pagination et compteur")
    class HistorySection {

        @Test
        @DisplayName("retourne l'historique paginé du repository")
        void returnsHistoryPaged() {
            Emprunt hist = createHistoryEntry(1L, "Bob", "1984", 60, 30);

            when(empruntRepository.findActiveLoans()).thenReturn(List.of());
            when(empruntRepository.findHistoryPaged(isNull(), isNull(), eq(QUERY_VALUE_TOUS), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of(hist)));
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_TOUS))).thenReturn(1L);

            LoanActivity activity = loanActivityService.getLoanActivity(null, null, 0, null, null);

            assertThat(activity.history()).hasSize(1);
            assertThat(activity.history().get(0).bookTitle()).isEqualTo("1984");
            assertThat(activity.history().get(0).status()).isEqualTo(LoanStatus.RETURNED);
        }

        @Test
        @DisplayName("filtre historique 'termines' est transmis au repository")
        void historyFilterTermines_forwardedToRepository() {
            Emprunt rendu = createHistoryEntry(2L, "Bob", "1984", 60, 5);
            rendu.setDateRetourPrevue(LocalDate.now().plusDays(10));

            when(empruntRepository.findActiveLoans()).thenReturn(List.of());
            when(empruntRepository.findHistoryPaged(isNull(), isNull(), eq(QUERY_VALUE_TERMINES), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of(rendu)));
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_TOUS))).thenReturn(1L);
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_TERMINES))).thenReturn(1L);

            LoanActivity activity = loanActivityService.getLoanActivity(null, null, 0, null, QUERY_VALUE_TERMINES);

            assertThat(activity.history()).hasSize(1);
            assertThat(activity.history().get(0).status()).isEqualTo(LoanStatus.RETURNED);
        }

        @Test
        @DisplayName("filtre historique 'rendus_en_retard' est transmis au repository")
        void historyFilterRendusEnRetard_forwardedToRepository() {
            Emprunt renduEnRetard = createHistoryEntry(3L, "Charlie", "Fondation", 60, 30);
            renduEnRetard.setDateRetourPrevue(LocalDate.now().minusDays(50));

            when(empruntRepository.findActiveLoans()).thenReturn(List.of());
            when(empruntRepository.findHistoryPaged(isNull(), isNull(), eq(QUERY_VALUE_RENDUS_EN_RETARD), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of(renduEnRetard)));
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_TOUS))).thenReturn(1L);
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_RENDUS_EN_RETARD))).thenReturn(1L);

            LoanActivity activity = loanActivityService.getLoanActivity(null, null, 0, null, QUERY_VALUE_RENDUS_EN_RETARD);

            assertThat(activity.history()).hasSize(1);
            assertThat(activity.history().get(0).status()).isEqualTo(LoanStatus.LATE_RETURN);
        }

        @Test
        @DisplayName("filtre actif n'affecte pas l'historique (toujours transmis au repository)")
        void activeFilter_doesNotAffectHistory() {
            Emprunt hist = createHistoryEntry(1L, "Alice", "Dune", 60, 30);

            when(empruntRepository.findActiveLoans()).thenReturn(List.of());
            when(empruntRepository.findHistoryPaged(isNull(), isNull(), eq(QUERY_VALUE_TOUS), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of(hist)));
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_TOUS))).thenReturn(1L);

            LoanActivity activity = loanActivityService.getLoanActivity(null, null, 0, QUERY_VALUE_EN_RETARD, null);

            assertThat(activity.history()).hasSize(1);
        }

        @Test
        @DisplayName("compteur de section historique = countHistoryFiltered avec 'tous', indépendant du filtre historique")
        void historySectionTotal_alwaysTous() {
            when(empruntRepository.findActiveLoans()).thenReturn(List.of());
            when(empruntRepository.findHistoryPaged(isNull(), isNull(), eq(QUERY_VALUE_TERMINES), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of()));
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_TOUS))).thenReturn(42L);
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_TERMINES))).thenReturn(7L);

            LoanActivity activity = loanActivityService.getLoanActivity(null, null, 0, null, QUERY_VALUE_TERMINES);

            assertThat(activity.totalElements()).isEqualTo(42L);
        }

        @Test
        @DisplayName("compteur de pagination = countHistoryFiltered avec le filtre historique courant")
        void paginationTotal_usesCurrentHistoryFilter() {
            when(empruntRepository.findActiveLoans()).thenReturn(List.of());
            when(empruntRepository.findHistoryPaged(isNull(), isNull(), eq(QUERY_VALUE_TERMINES), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of()));
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_TOUS))).thenReturn(42L);
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_TERMINES))).thenReturn(7L);

            LoanActivity activity = loanActivityService.getLoanActivity(null, null, 0, null, QUERY_VALUE_TERMINES);

            assertThat(activity.paginationTotal()).isEqualTo(7L);
        }
    }

    @Nested
    @DisplayName("Pagination")
    class Pagination {

        @Test
        @DisplayName("la pagination est déléguée au repository")
        void paginationHandledByRepository() {
            PageRequest pageRequest = PageRequest.of(2, 10);
            Emprunt hist = createHistoryEntry(1L, "Alice", "Dune", 60, 30);

            when(empruntRepository.findActiveLoans()).thenReturn(List.of());
            when(empruntRepository.findHistoryPaged(isNull(), isNull(), eq(QUERY_VALUE_TOUS), eq(pageRequest)))
                    .thenReturn(new PageImpl<>(List.of(hist), pageRequest, 25));
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_TOUS))).thenReturn(25L);

            LoanActivity activity = loanActivityService.getLoanActivity(null, null, 2, null, null);

            assertThat(activity.history()).hasSize(1);
            assertThat(activity.page()).isEqualTo(2);
            assertThat(activity.totalPages()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Recherche")
    class Search {

        @Test
        @DisplayName("recherche par utilisateur transmise au repository pour les actifs")
        void searchUser_forwardedToActiveRepository() {
            Emprunt actif = createActiveEntry(1L, "Alice", "Dune", 5);

            when(empruntRepository.findActiveLoansFiltered(eq("%ali%"), isNull())).thenReturn(List.of(actif));
            when(empruntRepository.findHistoryPaged(eq("%ali%"), isNull(), eq(QUERY_VALUE_TOUS), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of()));
            when(empruntRepository.countHistoryFiltered(eq("%ali%"), isNull(), eq(QUERY_VALUE_TOUS))).thenReturn(0L);

            LoanActivity activity = loanActivityService.getLoanActivity("Ali", null, 0, null, null);

            assertThat(activity.activeLoans()).hasSize(1);
            assertThat(activity.hasSearch()).isTrue();
        }

        @Test
        @DisplayName("recherche par utilisateur transmise au repository pour l'historique")
        void searchUser_forwardedToHistoryRepository() {
            Emprunt hist = createHistoryEntry(1L, "Alice", "Dune", 60, 30);

            when(empruntRepository.findActiveLoansFiltered(eq("%ali%"), isNull())).thenReturn(List.of());
            when(empruntRepository.findHistoryPaged(eq("%ali%"), isNull(), eq(QUERY_VALUE_TOUS), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of(hist)));
            when(empruntRepository.countHistoryFiltered(eq("%ali%"), isNull(), eq(QUERY_VALUE_TOUS))).thenReturn(1L);

            LoanActivity activity = loanActivityService.getLoanActivity("Ali", null, 0, null, null);

            assertThat(activity.history()).hasSize(1);
            assertThat(activity.totalElements()).isEqualTo(1L);
            assertThat(activity.hasSearch()).isTrue();
        }

        @Test
        @DisplayName("recherche blanche est ignorée : findActiveLoans (non filtré) est appelé")
        void blankSearch_ignored() {
            Emprunt actif = createActiveEntry(1L, "Alice", "Dune", 5);

            when(empruntRepository.findActiveLoans()).thenReturn(List.of(actif));
            when(empruntRepository.findHistoryPaged(isNull(), isNull(), eq(QUERY_VALUE_TOUS), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of()));
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_TOUS))).thenReturn(0L);

            LoanActivity activity = loanActivityService.getLoanActivity("   ", null, 0, null, null);

            assertThat(activity.activeLoans()).hasSize(1);
            assertThat(activity.hasSearch()).isFalse();
        }
    }

    @Nested
    @DisplayName("Données vides")
    class EmptyData {

        @Test
        @DisplayName("sans données, l'activité est vide")
        void handlesEmptyData() {
            when(empruntRepository.findActiveLoans()).thenReturn(List.of());
            when(empruntRepository.findHistoryPaged(isNull(), isNull(), eq(QUERY_VALUE_TOUS), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of()));
            when(empruntRepository.countHistoryFiltered(isNull(), isNull(), eq(QUERY_VALUE_TOUS))).thenReturn(0L);

            LoanActivity activity = loanActivityService.getLoanActivity(null, null, 0, null, null);

            assertThat(activity.activeLoans()).isEmpty();
            assertThat(activity.history()).isEmpty();
            assertThat(activity.activeTotalElements()).isEqualTo(0L);
            assertThat(activity.totalElements()).isEqualTo(0L);
            assertThat(activity.paginationTotal()).isEqualTo(0L);
            assertThat(activity.hasSearch()).isFalse();
        }
    }

    @Nested
    @DisplayName("Méthodes de comptage (inchangées)")
    class CountMethods {

        @Test
        @DisplayName("countActiveLoans délègue au repository")
        void countActiveLoans_delegatesToRepository() {
            when(empruntRepository.countByDateRetourIsNull()).thenReturn(5L);

            assertThat(loanActivityService.countActiveLoans()).isEqualTo(5L);
        }

        @Test
        @DisplayName("countOverdueLoans délègue au repository")
        void countOverdueLoans_delegatesToRepository() {
            when(empruntRepository.countByDateRetourIsNullAndDateRetourPrevueBefore(LocalDate.now()))
                    .thenReturn(3L);

            assertThat(loanActivityService.countOverdueLoans()).isEqualTo(3L);
        }
    }

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
