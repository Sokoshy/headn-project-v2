package com.bibliotheque.model.activite.filter;

import com.bibliotheque.model.Emprunt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ActiveLoanFilterTest {

    @Test
    @DisplayName("fromQueryParam(\"tous\") retourne All")
    void fromQueryParam_tous_retourneAll() {
        ActiveLoanFilter filter = ActiveLoanFilter.fromQueryParam("tous");

        assertThat(filter).isEqualTo(ActiveLoanFilter.All.INSTANCE);
    }

    @Test
    @DisplayName("fromQueryParam(\"actifs\") retourne Active")
    void fromQueryParam_actifs_retourneActive() {
        ActiveLoanFilter filter = ActiveLoanFilter.fromQueryParam("actifs");

        assertThat(filter).isEqualTo(ActiveLoanFilter.Active.INSTANCE);
    }

    @Test
    @DisplayName("fromQueryParam(\"en_retard\") retourne Overdue")
    void fromQueryParam_enRetard_retourneOverdue() {
        ActiveLoanFilter filter = ActiveLoanFilter.fromQueryParam("en_retard");

        assertThat(filter).isEqualTo(ActiveLoanFilter.Overdue.INSTANCE);
    }

    @Test
    @DisplayName("fromQueryParam(null) retourne All")
    void fromQueryParam_null_retourneAll() {
        ActiveLoanFilter filter = ActiveLoanFilter.fromQueryParam(null);

        assertThat(filter).isEqualTo(ActiveLoanFilter.All.INSTANCE);
    }

    @Test
    @DisplayName("fromQueryParam(\"\") retourne All")
    void fromQueryParam_chaineVide_retourneAll() {
        ActiveLoanFilter filter = ActiveLoanFilter.fromQueryParam("");

        assertThat(filter).isEqualTo(ActiveLoanFilter.All.INSTANCE);
    }

    @Test
    @DisplayName("fromQueryParam(\"inconnu\") retourne All par défaut")
    void fromQueryParam_valeurInconnue_retourneAll() {
        ActiveLoanFilter filter = ActiveLoanFilter.fromQueryParam("inconnu");

        assertThat(filter).isEqualTo(ActiveLoanFilter.All.INSTANCE);
    }

    @Nested
    @DisplayName("queryValue()")
    class QueryValue {

        @Test
        @DisplayName("All expose \"tous\"")
        void all_queryValue_retourneTous() {
            assertThat(ActiveLoanFilter.All.INSTANCE.queryValue()).isEqualTo("tous");
        }

        @Test
        @DisplayName("Active expose \"actifs\"")
        void active_queryValue_retourneActifs() {
            assertThat(ActiveLoanFilter.Active.INSTANCE.queryValue()).isEqualTo("actifs");
        }

        @Test
        @DisplayName("Overdue expose \"en_retard\"")
        void overdue_queryValue_retourneEnRetard() {
            assertThat(ActiveLoanFilter.Overdue.INSTANCE.queryValue()).isEqualTo("en_retard");
        }
    }

    @Nested
    @DisplayName("All.matches(Emprunt)")
    class AllMatches {

        @Test
        @DisplayName("emprunt actif (date retour null) → true")
        void empruntActif_retourneVrai() {
            Emprunt emprunt = empruntEnCours(LocalDate.now().plusDays(5));

            assertThat(ActiveLoanFilter.All.INSTANCE.matches(emprunt)).isTrue();
        }

        @Test
        @DisplayName("emprunt rendu (date retour non null) → false")
        void empruntRendu_retourneFaux() {
            Emprunt emprunt = empruntRendu(LocalDate.now().minusDays(10), LocalDate.now().minusDays(3), LocalDate.now().minusDays(2));

            assertThat(ActiveLoanFilter.All.INSTANCE.matches(emprunt)).isFalse();
        }
    }

    @Nested
    @DisplayName("Active.matches(Emprunt)")
    class ActiveMatches {

        @Test
        @DisplayName("emprunt actif non en retard (date prévue future) → true")
        void empruntActifNonEnRetard_retourneVrai() {
            Emprunt emprunt = empruntEnCours(LocalDate.now().plusDays(5));

            assertThat(ActiveLoanFilter.Active.INSTANCE.matches(emprunt)).isTrue();
        }

        @Test
        @DisplayName("emprunt actif non en retard (date prévue = aujourd'hui) → true")
        void empruntActifDatePrevueAujourdHui_retourneVrai() {
            Emprunt emprunt = empruntEnCours(LocalDate.now());

            assertThat(ActiveLoanFilter.Active.INSTANCE.matches(emprunt)).isTrue();
        }

        @Test
        @DisplayName("emprunt actif en retard (date prévue passée) → false")
        void empruntActifEnRetard_retourneFaux() {
            Emprunt emprunt = empruntEnCours(LocalDate.now().minusDays(1));

            assertThat(ActiveLoanFilter.Active.INSTANCE.matches(emprunt)).isFalse();
        }

        @Test
        @DisplayName("emprunt rendu → false")
        void empruntRendu_retourneFaux() {
            Emprunt emprunt = empruntRendu(LocalDate.now().minusDays(10), LocalDate.now().minusDays(3), LocalDate.now().minusDays(2));

            assertThat(ActiveLoanFilter.Active.INSTANCE.matches(emprunt)).isFalse();
        }
    }

    @Nested
    @DisplayName("Overdue.matches(Emprunt)")
    class OverdueMatches {

        @Test
        @DisplayName("emprunt actif en retard (date prévue passée) → true")
        void empruntActifEnRetard_retourneVrai() {
            Emprunt emprunt = empruntEnCours(LocalDate.now().minusDays(1));

            assertThat(ActiveLoanFilter.Overdue.INSTANCE.matches(emprunt)).isTrue();
        }

        @Test
        @DisplayName("emprunt actif non en retard (date prévue future) → false")
        void empruntActifNonEnRetard_retourneFaux() {
            Emprunt emprunt = empruntEnCours(LocalDate.now().plusDays(5));

            assertThat(ActiveLoanFilter.Overdue.INSTANCE.matches(emprunt)).isFalse();
        }

        @Test
        @DisplayName("emprunt rendu → false")
        void empruntRendu_retourneFaux() {
            Emprunt emprunt = empruntRendu(LocalDate.now().minusDays(10), LocalDate.now().minusDays(3), LocalDate.now().minusDays(2));

            assertThat(ActiveLoanFilter.Overdue.INSTANCE.matches(emprunt)).isFalse();
        }
    }

    private static Emprunt empruntEnCours(LocalDate dateRetourPrevue) {
        Emprunt emprunt = new Emprunt();
        emprunt.setDateEmprunt(LocalDate.now().minusDays(5));
        emprunt.setDateRetourPrevue(dateRetourPrevue);
        return emprunt;
    }

    private static Emprunt empruntRendu(LocalDate dateEmprunt, LocalDate dateRetour, LocalDate dateRetourPrevue) {
        Emprunt emprunt = new Emprunt();
        emprunt.setDateEmprunt(dateEmprunt);
        emprunt.setDateRetour(dateRetour);
        emprunt.setDateRetourPrevue(dateRetourPrevue);
        return emprunt;
    }
}
