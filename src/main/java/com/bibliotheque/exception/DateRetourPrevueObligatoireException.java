package com.bibliotheque.exception;

public class DateRetourPrevueObligatoireException extends BusinessException {

    public DateRetourPrevueObligatoireException() {
        super("La date de retour prévue est obligatoire.", "DATE_RETOUR_PREVUE_OBLIGATOIRE");
    }
}
