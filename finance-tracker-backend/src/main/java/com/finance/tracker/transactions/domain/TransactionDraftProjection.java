package com.finance.tracker.transactions.domain;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionDraftProjection {

    UUID getId();
    String getTransactionName();
    BigDecimal getAmount();
    @Enumerated(EnumType.STRING)
    TransactionType getType();
    String getAccountId();
    String getAccountName();
    String getCategoryId();
    String getCategoryName();
    Instant getOccurredAt();
    Instant getPostedAt();
    Currency getCurrency();
    TransactionStatus getStatus();
    List<String> getTags();
    String getOriginalMessage();
}
