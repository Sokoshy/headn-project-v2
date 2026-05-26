package com.bibliotheque.model.preparation;

import java.time.LocalDate;
import java.util.List;

public record LoanPreparation(
        List<SelectableBook> availableBooks,
        List<SelectableUser> users,
        LocalDate defaultExpectedReturnDate
) {}