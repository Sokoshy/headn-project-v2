package com.bibliotheque.model.activite;

import java.util.List;

public record LoanActivity(
        List<ActiveLoan> activeLoans,
        long activeTotalElements,
        List<LoanHistory> history,
        int page,
        int totalPages,
        long totalElements,
        long paginationTotal,
        boolean hasSearch
) {}
