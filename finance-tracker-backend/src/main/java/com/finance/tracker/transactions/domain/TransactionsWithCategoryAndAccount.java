package com.finance.tracker.transactions.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Builder
public class TransactionsWithCategoryAndAccount {

    private String id;

    private String transactionName;
    private Double amount;
    private String type;
    private String accountId;
    private String accountName;
    private BigDecimal balanceCached;

    private String categoryId;
    private String categoryName;
    private Timestamp occuredAt;
    private Timestamp postedAt;
    private String currency;

    public TransactionsWithCategoryAndAccount(String id, String transactionName, Double amount, String type, String accountId, String accountName, BigDecimal balanceCached, String categoryId, String categoryName, Timestamp occuredAt, Timestamp postedAt, String currency) {
        this.id = id;
        this.transactionName = transactionName;
        this.amount = amount;
        this.type = type;
        this.accountId = accountId;
        this.accountName = accountName;
        this.balanceCached = balanceCached;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.occuredAt = occuredAt;
        this.postedAt = postedAt;
        this.currency = currency;
    }
}
