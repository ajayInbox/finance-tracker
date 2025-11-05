package com.finance.tracker.transactions.domain;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReconciliationCreateEvent extends ApplicationEvent {

    private final String originalTxnId;
    private final String reversalTxnId;
    private final String updatedTxnId;

    public ReconciliationCreateEvent(Object source, String originalTxnId,
                                     String reversalTxnId, String updatedTxnId) {
        super(source);
        this.originalTxnId=originalTxnId;
        this.reversalTxnId=reversalTxnId;
        this.updatedTxnId=updatedTxnId;
    }
}
