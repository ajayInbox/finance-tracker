package com.finance.tracker.transactions.domain.dtos;

import java.math.BigDecimal;
import java.util.List;

public record CreateTransactionRequestDto(
        BigDecimal amount,
        String type,
        String merchant,
        String notes,
        String transactionName,
        String account,
        String category,
        List<String> tags,
        String occurredAt,
        String postedAt,
        String currency,
        String attachments,
        String externalRef
) {
}
