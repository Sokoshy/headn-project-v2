package com.bibliotheque.model.activite.filter;

import com.bibliotheque.model.Emprunt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class HistoryFilterTest {

    @Test
    @DisplayName("fromQueryParam(\"tous\") retourne All")
    void fromQueryParam_tous_retourneAll() {
        HistoryFilter filter = HistoryFilter.fromQueryParam("tous");

        assertThat(filter).isEqualTo(HistoryFilter.All.INSTANCE);
    }

    @Test
    @DisplayName("fromQueryParam(\"termines\") retourne Completed")
    void fromQueryParam_termines_retourneCompleted() {
        HistoryFilter filter = HistoryFilter.fromQueryParam("termines");

        assertThat(filter).isEqualTo(HistoryFilter.Completed.INSTANCE);
    }

    @Test
    @DisplayName("fromQueryParam(\"rendus_en_retard\") retourne LateReturn")
    void fromQueryParam_rendusEnRetard_retourneLateReturn() {
        HistoryFilter filter = HistoryFilter.fromQueryParam("rendus_en_retard");

        assertThat(filter).isEqualTo(HistoryFilter.LateReturn.INSTANCE);
    }

    @Test
    @DisplayName("fromQueryParam(null) retourne All")
    void fromQueryParam_null_retourneAll() {
        HistoryFilter filter = HistoryFilter.fromQueryParam(null);

        assertThat(filter).isEqualTo(HistoryFilter.All.INSTANCE);
    }

    @Test
    @DisplayName("fromQueryParam(\"\") retourne All")
    void fromQueryParam_chaineVide_retourneAll() {
        HistoryFilter filter = HistoryFilter.fromQueryParam("");

        assertThat(filter).isEqualTo(HistoryFilter.All.INSTANCE);
    }

    @Test
    @DisplayName("fromQueryParam(\"inconnu\") retourne All par défaut")
    void fromQueryParam_valeurInconnue_retourneAll() {
        HistoryFilter filter = HistoryFilter.fromQueryParam("inconnu");

        assertThat(filter).isEqualTo(HistoryFilter.All.INSTANCE);
    }

    @Nested
    @DisplayName("queryValue()")
    class QueryValue {

        @Test
        @DisplayName("All expose \"tous\"")
        void all_queryValue_retourneTous() {
            assertThat(HistoryFilter.All.INSTANCE.queryValue()).isEqualTo("tous");
        }

        @Test
        @DisplayName("Completed expose \"termines\"")
        void completed_queryValue_retourneTermines() {
            assertThat(HistoryFilter.Completed.INSTANCE.queryValue()).isEqualTo("termines");
        }

        @Test
        @DisplayName("LateReturn expose \"rendus_en_retard\"")
        void lateReturn_queryValue_retourneRendusEnRetard() {
            assertThat(HistoryFilter.LateReturn.INSTANCE.queryValue()).isEqualTo("rendus_en_retard");
        }
    }

    @Nested
    @DisplayName("All.matches(Emprunt)")
    class AllMatches {

        @Test
        @DisplayName("emprunt rendu (date retour non null) → true")
        void empruntRendu_retourneVrai() {
            Emprunt emprunt = empruntRendu(LocalDate.now().minusDays(30), LocalDate.now().minusDays(3), LocalDate.now().minusDays(2));

            assertThat(HistoryFilter.All.INSTANCE.matches(emprunt)).isTrue();
        }

        @Test
        @DisplayName("emprunt actif (date retour null) → false")
        void empruntActif_retourneFaux() {
            Emprunt emprunt = empruntEnCours(LocalDate.now().plusDays(5));

            assertThat(HistoryFilter.All.INSTANCE.matches(emprunt)).isFalse();
        }
    }

    @Nested
    @DisplayName("Completed.matches(Emprunt)")
    class CompletedMatches {

        @Test
        @DisplayName("emprunt rendu à l'heure (date retour = date prévue) → true")
        void empruntRenduALHeure_retourneVrai() {
            LocalDate dateCommune = LocalDate.now().minusDays(2);
            Emprunt emprunt = empruntRendu(LocalDate.now().minusDays(30), dateCommune, dateCommune);

            assertThat(HistoryFilter.Completed.INSTANCE.matches(emprunt)).isTrue();
        }

        @Test
        @DisplayName("emprunt rendu en avance (date retour < date prévue) → true")
        void empruntRenduEnAvance_retourneVrai() {
            Emprunt emprunt = empruntRendu(
                    LocalDate.now().minusDays(30),
                    LocalDate.now().minusDays(5),
                    LocalDate.now().minusDays(1)
            );

            assertThat(HistoryFilter.Completed.INSTANCE.matches(emprunt)).isTrue();
        }

        @Test
        @DisplayName("emprunt rendu en retard (date retour > date prévue) → false")
        void empruntRenduEnRetard_retourneFaux() {
            Emprunt emprunt = empruntRendu(
                    LocalDate.now().minusDays(30),
                    LocalDate.now().minusDays(1),
                    LocalDate.now().minusDays(5)
            );

            assertThat(HistoryFilter.Completed.INSTANCE.matches(emprunt)).isFalse();
        }

        @Test
        @DisplayName("emprunt actif → false")
        void empruntActif_retourneFaux() {
            Emprunt emprunt = empruntEnCours(LocalDate.now().plusDays(5));

            assertThat(HistoryFilter.Completed.INSTANCE.matches(emprunt)).isFalse();
        }
    }

    @Nested
    @DisplayName("LateReturn.matches(Emprunt)")
    class LateReturnMatches {

        @Test
        @DisplayName("emprunt rendu en retard (date retour > date prévue) → true")
        void empruntRenduEnRetard_retourneVrai() {
            Emprunt emprunt = empruntRendu(
                    LocalDate.now().minusDays(30),
                    LocalDate.now().minusDays(1),
                    LocalDate.now().minusDays(5)
            );

            assertThat(HistoryFilter.LateReturn.INSTANCE.matches(emprunt)).isTrue();
        }

        @Test
        @DisplayName("emprunt rendu à l'heure (date retour = date prévue) → false")
        void empruntRenduALHeure_retourneFaux() {
            LocalDate dateCommune = LocalDate.now().minusDays(2);
            Emprunt emprunt = empruntRendu(LocalDate.now().minusDays(30), dateCommune, dateCommune);

            assertThat(HistoryFilter.LateReturn.INSTANCE.matches(emprunt)).isFalse();
        }

        @Test
        @DisplayName("emprunt rendu en avance (date retour < date prévue) → false")
        void empruntRenduEnAvance_retourneFaux() {
            Emprunt emprunt = empruntRendu(
                    LocalDate.now().minusDays(30),
                    LocalDate.now().minusDays(5),
                    LocalDate.now().minusDays(1)
            );

            assertThat(HistoryFilter.LateReturn.INSTANCE.matches(emprunt)).isFalse();
        }

        @Test
        @DisplayName("emprunt actif → false")
        void empruntActif_retourneFaux() {
            Emprunt emprunt = empruntEnCours(LocalDate.now().plusDays(5));

            assertThat(HistoryFilter.LateReturn.INSTANCE.matches(emprunt)).isFalse();
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
