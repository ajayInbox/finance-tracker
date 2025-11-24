package com.finance.tracker.transactions.service.impl;

import com.finance.tracker.accounts.domain.BalanceUpdateRequest;
import com.finance.tracker.accounts.service.AccountService;
import com.finance.tracker.transactions.domain.*;
import com.finance.tracker.transactions.domain.entities.Transaction;
import com.finance.tracker.transactions.exceptions.TransactionNotFoundException;
import com.finance.tracker.transactions.repository.TransactionRepository;
import com.finance.tracker.transactions.service.TransactionReversalService;
import com.finance.tracker.transactions.service.TransactionValidationService;
import com.finance.tracker.transactions.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TransactionReversalServiceImpl implements TransactionReversalService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final TransactionValidationService validationService;
    private final TransactionMapper transactionMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public String deleteTransaction(Transaction transaction) {

        if (transaction.getLastAction() == LastAction.DELETED
                || transaction.getLastAction() == LastAction.PARTIAL_DELETED) {
            return "Transaction already Deleted";
        }

        // Build reversal transaction
        Transaction reversal = buildReversalTransaction(transaction);

        // 1. Mark original as partially deleted
        transaction.setLastAction(LastAction.PARTIAL_DELETED);
        transaction.setStatus(TransactionStatus.INACTIVE);
        transaction.setTransactionName(transaction.getTransactionName() + " (Deleted)");
        Transaction savedOriginal = transactionRepository.save(transaction);

        // 2. Save reversal and update balance
        Transaction savedReversal = transactionRepository.save(reversal);
        updateBalanceFor(savedReversal);

        // 3. Publish reconciliation event
        eventPublisher.publishEvent(
                new ReconciliationCreateEvent(
                        this,
                        savedOriginal.getId(),
                        savedReversal.getId(),
                        null
                )
        );

        return "Transaction Deleted Successfully";
    }

    @Transactional
    @Override
    public Transaction updateTransaction(String transactionId, UpdateTransactionRequest request) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("not found"));

        validationService.validate(request);

        boolean amountChanged = !Objects.equals(transaction.getAmount(), request.getAmount());
        boolean typeChanged = !Objects.equals(
                transaction.getType(), TransactionType.fromValueIgnoreCase(request.getType())
        );
        boolean accountChanged = !Objects.equals(transaction.getAccount(), request.getAccount());

        // If structural fields changed, do reversal workflow
        if (amountChanged || typeChanged || accountChanged) {
            // Build reversal transaction
            Transaction reversal = buildReversalTransaction(transaction);
            // 2. Save reversal and update balance
            Transaction savedReversal = transactionRepository.save(reversal);
            updateBalanceFor(savedReversal);

            // new txn from request
            // TODO: wire proper userId
            String userId = null;
            Transaction newTxn = transactionMapper.toNewEntity(request, userId);
            Transaction savedNewTxn = transactionRepository.save(newTxn);
            updateBalanceFor(savedNewTxn);

            transaction.setLastAction(LastAction.REPLACED);
            transaction.setUpdatedAt(Instant.now());
            transaction.setStatus(TransactionStatus.INACTIVE);
            Transaction savedOriginal = transactionRepository.save(transaction);

            eventPublisher.publishEvent(
                    new ReconciliationCreateEvent(
                            this,
                            savedOriginal.getId(),
                            savedReversal.getId(),
                            savedNewTxn.getId()
                    )
            );

            return savedNewTxn;
        }

        // Only non-structural changes
        String userId = null;
        transactionMapper.updateNonStructuralFields(transaction, request, userId);
        return transactionRepository.save(transaction);
    }

    private void updateBalanceFor(Transaction txn) {
        accountService.updateBalanceForTransaction(
                new BalanceUpdateRequest(
                        txn.getAccount(),
                        txn.getAmount(),
                        txn.getType(),
                        txn.getId()
                )
        );
    }

    private Transaction buildReversalTransaction(Transaction transaction) {
        Transaction t = new Transaction(transaction);
        t.setType(getReversalType(transaction.getType()));
        t.setTransactionName(transaction.getTransactionName()+ " (Reversal)");
        t.setLastAction(LastAction.REVERSAL);
        t.setCreatedAt(Instant.now());
        t.setUpdatedAt(Instant.now());
        t.setStatus(TransactionStatus.REVERSAL);
        t.setReversalOf(transaction.getId());
        return t;
    }

    private TransactionType getReversalType(TransactionType originalType) {
        if (originalType == null) return TransactionType.UNKNOWN;
        return switch (originalType) {
            case EXPENSE -> TransactionType.INCOME;
            case INCOME -> TransactionType.EXPENSE;
            default -> originalType;
        };
    }

}
