package com.finance.tracker.accounts.domain;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class ATSnapshotCreateEvent extends ApplicationEvent {

    private final String accountId;
    private final String transactionId;
    private final BigDecimal previousBalance;
    private final BigDecimal newBalance;
    private final BigDecimal transactionAmount;

    public ATSnapshotCreateEvent(Object source, String accountId, String transactionId,
                                 BigDecimal previousBalance, BigDecimal newBalance, BigDecimal transactionAmount) {
        super(source);
        this.accountId=accountId;
        this.transactionId=transactionId;
        this.newBalance=newBalance;
        this.previousBalance=previousBalance;
        this.transactionAmount=transactionAmount;
    }
}
