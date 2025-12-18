package com.finance.tracker.transactions.domain;

public record CategoryExpenseSummary (

        String categoryId,
        String categoryName,
        Double total,
        Long transactionCount
){}
