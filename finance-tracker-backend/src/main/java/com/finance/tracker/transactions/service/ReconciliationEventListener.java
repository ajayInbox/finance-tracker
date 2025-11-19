package com.finance.tracker.transactions.service;

import com.finance.tracker.transactions.domain.ReconciliationCreateEvent;
import com.finance.tracker.transactions.domain.ReconciliationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class ReconciliationEventListener {

    private final ReconciliationService service;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleReconciliationCreation(ReconciliationCreateEvent event){

        ReconciliationRequest req = ReconciliationRequest.builder()
                .originalTxnId(event.getOriginalTxnId())
                .reversalTxnId(event.getReversalTxnId())
                .updatedTxnId(event.getUpdatedTxnId()) // null is fine
                .build();

        service.addEntry(req);
    }
}
