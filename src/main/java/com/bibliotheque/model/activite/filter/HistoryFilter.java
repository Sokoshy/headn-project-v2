package com.bibliotheque.model.activite.filter;

import com.bibliotheque.model.Emprunt;

public sealed interface HistoryFilter permits HistoryFilter.All, HistoryFilter.Completed, HistoryFilter.LateReturn {

    String queryValue();

    boolean matches(Emprunt emprunt);

    static HistoryFilter fromQueryParam(String value) {
        if (value == null) return All.INSTANCE;
        return switch (value) {
            case "termines" -> Completed.INSTANCE;
            case "rendus_en_retard" -> LateReturn.INSTANCE;
            default -> All.INSTANCE;
        };
    }

    record All() implements HistoryFilter {
        public static final All INSTANCE = new All();

        @Override
        public String queryValue() {
            return "tous";
        }

        @Override
        public boolean matches(Emprunt emprunt) {
            return !emprunt.estEnCours();
        }
    }

    record Completed() implements HistoryFilter {
        public static final Completed INSTANCE = new Completed();

        @Override
        public String queryValue() {
            return "termines";
        }

        @Override
        public boolean matches(Emprunt emprunt) {
            return !emprunt.estEnCours() && !emprunt.estRenduEnRetard();
        }
    }

    record LateReturn() implements HistoryFilter {
        public static final LateReturn INSTANCE = new LateReturn();

        @Override
        public String queryValue() {
            return "rendus_en_retard";
        }

        @Override
        public boolean matches(Emprunt emprunt) {
            return emprunt.estRenduEnRetard();
        }
    }
}
