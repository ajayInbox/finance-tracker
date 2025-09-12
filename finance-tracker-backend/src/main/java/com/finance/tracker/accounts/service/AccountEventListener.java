package com.finance.tracker.accounts.service;

import com.finance.tracker.accounts.domain.BalanceUpdateRequest;
import com.finance.tracker.transactions.domain.TransactionCreateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountEventListener {

    private final AccountService accountService;

    @EventListener
    @Transactional
    public void handleTransactionCreated(TransactionCreateEvent event) {
        // Update account balance based on transaction
        accountService.updateBalanceForTransaction(
                new BalanceUpdateRequest(
                        event.getAccountId(),
                        event.getAmount(),
                        event.getTransactionType(),
                        event.getTransactionId()
                )
        );
    }
}
