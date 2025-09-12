package com.finance.tracker.accounts.domain;

public record AccountCreateUpdateRequest(

        String label,
        String accountType,
        String currency,

        String cardNetwork,
        String lastFour,
        String statementDay,
        String paymentDueDay,
        Double creditLimit,
        Double openingBalance

) {
}
