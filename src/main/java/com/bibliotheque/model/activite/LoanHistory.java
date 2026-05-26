package com.bibliotheque.model.activite;

import java.time.LocalDate;

public record LoanHistory(
        Long empruntId,
        Long bookId,
        String bookTitle,
        Long userId,
        String userName,
        LocalDate borrowDate,
        LocalDate returnDate
) {}