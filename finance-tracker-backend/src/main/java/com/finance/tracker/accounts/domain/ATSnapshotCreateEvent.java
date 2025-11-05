package com.finance.tracker.accounts.domain;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ATSnapshotCreateEvent extends ApplicationEvent {

    private final String accountId;
    private final String transactionId;
    private final double previousBalance;
    private final double newBalance;
    private final double transactionAmount;

    public ATSnapshotCreateEvent(Object source, String accountId, String transactionId,
                                 double previousBalance, double newBalance, double transactionAmount) {
        super(source);
        this.accountId=accountId;
        this.transactionId=transactionId;
        this.newBalance=newBalance;
        this.previousBalance=previousBalance;
        this.transactionAmount=transactionAmount;
    }
}
