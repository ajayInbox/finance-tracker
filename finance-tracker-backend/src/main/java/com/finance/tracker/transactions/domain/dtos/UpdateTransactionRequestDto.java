package com.finance.tracker.transactions.domain.dtos;

import java.math.BigDecimal;
import java.util.List;

public record UpdateTransactionRequestDto (
        BigDecimal amount,
        String type,
        String account,
        String category,
        String merchant,
        String notes,
        String transactionName,
        List<String> tags,
        String occurredAt,
        String postedAt,
        String currency,
        String attachments,
        String externalRef
){}
