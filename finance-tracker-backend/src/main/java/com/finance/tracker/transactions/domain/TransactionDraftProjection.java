package com.finance.tracker.transactions.domain;

import java.math.BigDecimal;
import java.time.Instant;

public interface TransactionDraftProjection {

    String getId();
    String getTransactionName();
    BigDecimal getAmount();
    String getType();
    String getAccountId();
    String getAccountName();
    String getCategoryId();
    String getCategoryName();
    Instant getOccurredAt();
    Instant getPostedAt();
    String getCurrency();
    String getOriginalMessage();
}
