package com.bibliotheque.model.activite;

import java.util.List;

public record LoanActivity(
        List<ActiveLoan> activeLoans,
        List<LoanHistory> history,
        int page,
        int totalPages,
        long totalElements,
        boolean hasSearch
) {}