package com.finance.tracker.transactions.domain;

import java.math.BigDecimal;

public record CategoryExpenseSummary (

        String categoryId,
        String categoryName,
        BigDecimal total,
        Long transactionCount
){}
