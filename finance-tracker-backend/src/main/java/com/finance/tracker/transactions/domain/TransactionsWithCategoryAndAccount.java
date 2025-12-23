package com.finance.tracker.transactions.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

@Data
@Builder
public class TransactionsWithCategoryAndAccount {

    private String id;

    private String transactionName;
    private BigDecimal amount;
    private String type;
    private String accountId;
    private String accountName;
    private BigDecimal balanceCached;

    private String categoryId;
    private String categoryName;
    private Instant occurredAt;
    private Instant postedAt;
    private String currency;

    public TransactionsWithCategoryAndAccount(String id, String transactionName, BigDecimal amount, String type, String accountId, String accountName, BigDecimal balanceCached, String categoryId, String categoryName, Instant occurredAt, Instant postedAt, String currency) {
        this.id = id;
        this.transactionName = transactionName;
        this.amount = amount;
        this.type = type;
        this.accountId = accountId;
        this.accountName = accountName;
        this.balanceCached = balanceCached;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.occurredAt = occurredAt;
        this.postedAt = postedAt;
        this.currency = currency;
    }
}
