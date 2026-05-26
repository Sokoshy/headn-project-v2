package com.bibliotheque.exception;

public class DateRetourPrevueDansLePasseException extends BusinessException {

    public DateRetourPrevueDansLePasseException() {
        super("La date de retour prévue doit être aujourd'hui ou une date future.", "DATE_RETOUR_PREVUE_DANS_LE_PASSE");
    }
}
