package com.finance.tracker.transactions.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryExpenseSummary (

        UUID categoryId,
        String categoryName,
        BigDecimal total,
        Long transactionCount
){}
