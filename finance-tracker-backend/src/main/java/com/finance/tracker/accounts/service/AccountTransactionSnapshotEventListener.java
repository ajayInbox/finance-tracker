package com.finance.tracker.accounts.service;

import com.finance.tracker.accounts.domain.ATSnapshotCreateEvent;
import com.finance.tracker.accounts.domain.SnapshotCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class AccountTransactionSnapshotEventListener {

    private final AccountTransactionSnapshotService snapshotService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleAccountTransactionSnapshotCreation(ATSnapshotCreateEvent event){

        snapshotService.createSnapshot(
                new SnapshotCreateRequest(
                        event.getAccountId(),
                        event.getTransactionId(),
                        event.getPreviousBalance(),
                        event.getNewBalance(),
                        event.getTransactionAmount()
                )
        );
    }
}
