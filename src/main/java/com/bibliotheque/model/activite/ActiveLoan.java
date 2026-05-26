package com.bibliotheque.model.activite;

import java.time.LocalDate;

public record ActiveLoan(
        Long empruntId,
        Long bookId,
        String bookTitle,
        Long userId,
        String userName,
        LocalDate borrowDate,
        LocalDate expectedReturnDate,
        LoanStatus status
) {}