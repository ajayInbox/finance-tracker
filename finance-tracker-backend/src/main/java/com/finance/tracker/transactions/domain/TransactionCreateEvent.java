package com.finance.tracker.transactions.domain;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class TransactionCreateEvent extends ApplicationEvent {

    private String transactionId;
    private String accountId;
    private Double amount;
    private TransactionType transactionType;
    private LocalDateTime occuredAt;

    public TransactionCreateEvent(Object source, String transactionId,
                                  String accountId, Double amount, TransactionType transactionType,
                                  LocalDateTime occuredAt) {
        super(source);
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.occuredAt = occuredAt;
    }


}
