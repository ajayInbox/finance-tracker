package com.finance.tracker.transactions.service;

import com.finance.tracker.transactions.domain.ReconciliationCreateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReconciliationEventListener {

    private final ReconciliationService service;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleReconciliationCreation(ReconciliationCreateEvent event){
        service.addEntry(
                Map.of(
                        "reversalTxnId", event.getReversalTxnId(),
                        "originalTxnId", event.getOriginalTxnId(),
                        "updatedTxnId", event.getUpdatedTxnId()
                )
        );
    }
}
