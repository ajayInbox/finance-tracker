package com.finance.tracker.transactions.domain.dtos;

public record TransactionCreateUpdateRequestDto(
        Double transactionAmount,
        String transactionRemark
) {
}
