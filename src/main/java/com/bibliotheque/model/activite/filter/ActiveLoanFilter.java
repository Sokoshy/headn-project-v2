package com.bibliotheque.model.activite.filter;

import com.bibliotheque.model.Emprunt;

public sealed interface ActiveLoanFilter permits ActiveLoanFilter.All, ActiveLoanFilter.Active, ActiveLoanFilter.Overdue {

    String queryValue();

    boolean matches(Emprunt emprunt);

    static ActiveLoanFilter fromQueryParam(String value) {
        if (value == null) return All.INSTANCE;
        return switch (value) {
            case "actifs" -> Active.INSTANCE;
            case "en_retard" -> Overdue.INSTANCE;
            default -> All.INSTANCE;
        };
    }

    record All() implements ActiveLoanFilter {
        public static final All INSTANCE = new All();

        @Override
        public String queryValue() {
            return "tous";
        }

        @Override
        public boolean matches(Emprunt emprunt) {
            return emprunt.estEnCours();
        }
    }

    record Active() implements ActiveLoanFilter {
        public static final Active INSTANCE = new Active();

        @Override
        public String queryValue() {
            return "actifs";
        }

        @Override
        public boolean matches(Emprunt emprunt) {
            return emprunt.estEnCours() && !emprunt.estEnRetard();
        }
    }

    record Overdue() implements ActiveLoanFilter {
        public static final Overdue INSTANCE = new Overdue();

        @Override
        public String queryValue() {
            return "en_retard";
        }

        @Override
        public boolean matches(Emprunt emprunt) {
            return emprunt.estEnCours() && emprunt.estEnRetard();
        }
    }
}
