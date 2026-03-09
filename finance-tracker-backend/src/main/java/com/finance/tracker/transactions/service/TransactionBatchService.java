package com.finance.tracker.transactions.service;

import com.finance.tracker.accounts.domain.BalanceUpdateRequest;
import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.accounts.service.AccountService;
import com.finance.tracker.category.domain.CategoryType;
import com.finance.tracker.category.domain.entities.Category;
import com.finance.tracker.category.service.CategoryService;
import com.finance.tracker.transactions.domain.Currency;
import com.finance.tracker.transactions.domain.TransactionStatus;
import com.finance.tracker.transactions.domain.TransactionType;
import com.finance.tracker.transactions.domain.dtos.BatchUpdateTransactionRequestDto;
import com.finance.tracker.transactions.domain.entities.Transaction;
import com.finance.tracker.transactions.repository.TransactionBatchRepository;
import com.finance.tracker.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class TransactionBatchService {

    private static final int CHUNK_SIZE = 5;

    private final TransactionBatchRepository batchRepository;
    private final TransactionValidationService validationService;
    private final PlatformTransactionManager transactionManager;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final CategoryService categoryService;

    public void batchConfirmAndUpdate(UUID userId, List<BatchUpdateTransactionRequestDto> requests) {

        for (int i = 0; i < requests.size(); i += CHUNK_SIZE) {

            List<BatchUpdateTransactionRequestDto> chunk =
                    requests.subList(i, Math.min(i + CHUNK_SIZE, requests.size()));

            processChunk(userId, chunk);
        }
    }

    private void processChunk(UUID userId, List<BatchUpdateTransactionRequestDto> chunk) {

        TransactionTemplate txTemplate =
                new TransactionTemplate(transactionManager);

        txTemplate.execute(status -> {

            List<Transaction> transactions = new ArrayList<>();

            // Step 1: Validate each request
            for (BatchUpdateTransactionRequestDto req : chunk) {
                Transaction trnx = newEntity(req);
                Category category = categoryService.validateAndGet(userId, req.categoryId(), CategoryType.fromValueIgnoreCase(req.type()));
                Account account = accountService.getAccountByIdAndUser(req.accountId(), userId);
                trnx.setCategory(category);
                trnx.setAccount(account);
                transactions.add(trnx);
            }

            // Step 2: Batch update transaction table
            batchRepository.batchUpdateAndConfirm(transactions);

            // Step 3: Fetch updated transactions
            List<UUID> ids = transactions.stream()
                    .map(Transaction::getId)
                    .toList();

            List<Transaction> updatedTransactions =
                    transactionRepository.findAllById(ids);

            // Step 4: Update balance
            for (Transaction txn : updatedTransactions) {

                accountService.updateBalanceForTransaction(
                        new BalanceUpdateRequest(
                                txn.getAccount().getId(),
                                txn.getAmount(),
                                txn.getType(),
                                txn.getId()
                        ), userId
                );
            }

            return null;
        });
    }

    private Transaction newEntity(BatchUpdateTransactionRequestDto req) {
        return Transaction.builder()
                .id(req.id())
                .transactionName(req.transactionName())
                .notes(req.notes())
                .amount(req.amount())
                .merchant(req.merchant())
                .type(TransactionType.fromValueIgnoreCase(req.type()))
                .occurredAt(OffsetDateTime.of(req.occurredAt(), ZoneOffset.UTC))
                .tags(req.tags())
                .currency(Currency.valueOf(req.currency()))
                .status(TransactionStatus.CONFIRMED)
                .build();
    }
}
