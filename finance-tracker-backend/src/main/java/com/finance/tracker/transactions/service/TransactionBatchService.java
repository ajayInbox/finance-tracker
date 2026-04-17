package com.finance.tracker.transactions.service;

import com.finance.tracker.accounts.domain.AccountType;
import com.finance.tracker.accounts.domain.BalanceUpdateRequest;
import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.accounts.service.AccountService;
import com.finance.tracker.category.domain.CategoryType;
import com.finance.tracker.category.domain.entities.Category;
import com.finance.tracker.category.service.CategoryService;
import com.finance.tracker.sync.domain.EndScanRequest;
import com.finance.tracker.sync.exceptions.InvalidSyncRequestException;
import com.finance.tracker.sync.service.SyncService;
import com.finance.tracker.transactions.domain.*;
import com.finance.tracker.transactions.domain.Currency;
import com.finance.tracker.transactions.domain.dtos.BatchUpdateTransactionRequestDto;
import com.finance.tracker.transactions.domain.entities.Transaction;
import com.finance.tracker.transactions.repository.TransactionBatchRepository;
import com.finance.tracker.transactions.repository.TransactionRepository;
import com.finance.tracker.transactions.utilities.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionBatchService {

    private static final int CHUNK_SIZE = 5;

    private final TransactionBatchRepository batchRepository;
    private final PlatformTransactionManager transactionManager;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final TransactionSmsService smsService;
    private final TransactionAuditService auditService;
    private final SyncService syncService;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

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

    @Transactional
    public BatchSyncResponse processBatch(UUID userId, BatchSyncRequest request) {
        if (request == null) {
            throw new InvalidSyncRequestException("Batch request is required");
        }
        if (request.scanId() == null) {
            throw new InvalidSyncRequestException("scanId is required");
        }
        if (request.smsList() == null) {
            throw new InvalidSyncRequestException("smsList is required");
        }

        long startTime = System.currentTimeMillis();

        // 1. Bulk Deduplication
        Map<String, SmsRequest> incomingMap = request.smsList().stream()
                .collect(Collectors.toMap(this::generateSecureId, m -> m, (a, b) -> a));

        Set<String> existingIds = transactionRepository.findExistingIdentifiers(incomingMap.keySet());

        List<SmsRequest> newMessages = incomingMap.entrySet().stream()
                .filter(entry -> !existingIds.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();

        int duplicates = request.smsList().size() - newMessages.size();

        // 2. Parallel Processing with Virtual Threads
        // We use a thread-safe list to collect results
        List<Transaction> transactionsToSave = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger failedToParse = new AtomicInteger(0);

        List<CompletableFuture<Transaction>> futures = newMessages.stream()
                .map(msg -> CompletableFuture.supplyAsync(() -> {
                    String uid = generateSecureId(msg);
                    return smsService.parseTransactionFromSms(msg)
                            .map(parsed -> getDraftTransaction(userId, uid, parsed, msg.getBody(), request.scanId()))
                            .orElseGet(() -> {
                                failedToParse.incrementAndGet();
                                auditService.logFailedParsing(msg, "NO_REGEX_MATCH");
                                return null;
                            });
                }, executor))
                .toList();

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .forEach(transactionsToSave::add);

        // 3. Optimized Batch Save
        if (!transactionsToSave.isEmpty()) {
            // Ensure spring.jpa.properties.hibernate.jdbc.batch_size=50 is set
            transactionRepository.saveAll(transactionsToSave);
        }

        // 4. AUTOMATIC END SCAN: Update History
        EndScanRequest endScanRequest = EndScanRequest.builder()
                .transactionsCreated(transactionsToSave.size())
                .duplicatesSkipped(duplicates)
                .totalSmsProcessed(request.smsList().size())
                .failedToParse(failedToParse.get())
                .build();

        syncService.finalizeScan(
                userId, request.scanId(),
                endScanRequest
        );

        // 5. UPDATE METADATA: Move the "Bookmark" forward
        // Use the timestamp of the latest SMS in the batch (regardless of if it parsed)
        long latestTimestamp = request.smsList().stream()
                .mapToLong(SmsRequest::getTimestamp)
                .max()
                .orElse(0L);

        if (latestTimestamp > 0) {
            syncService.updateMetadata(userId, latestTimestamp);
        }

        log.info("Scan {} completed: {} new txns, {} ms", request.scanId(), transactionsToSave.size(), (System.currentTimeMillis() - startTime));

        return new BatchSyncResponse(
                transactionsToSave.size(),
                duplicates,
                failedToParse.get()
        );
    }

    private String generateSecureId(SmsRequest message) {
        // SHA-256 is more production-grade than MD5 for collisions
        return DigestUtils.md5DigestAsHex((message.getBody() + message.getTimestamp()).getBytes());
    }

    private Transaction getDraftTransaction(UUID userId, String uid, ParsedTransaction pt, String raw, UUID scanId) {
        BigDecimal amount = new BigDecimal(pt.getAmount().replace(",", ""));

        return Transaction.builder()
                .amount(amount)
                .createdAt(Instant.now())
                .currency(Currency.INR)
                .transactionName("Auto-detected Transaction")
                .status(TransactionStatus.DRAFT)
                .lastAction("CREATED")
                .userId(userId)
                .type(TransactionType.UNKNOWN)
                // Ensure occurredAt is parsed using a consistent formatter or fallback to now
                .occurredAt(safeParseDateTime(pt.getDateTime()))
                .postedAt(OffsetDateTime.now())
                .merchant(pt.getMerchant() != null ? pt.getMerchant() : "Unknown Merchant")
                .source(TransactionSource.SMS)
                .uniqueIdentifier(uid)
                .originalMessage(raw)
                .externalRef(String.format("Scan Id: %s", scanId))
                .account(Account.builder().id(Constants.DUMMY_ACCOUNT_ID).lastFour("0000").accountType(AccountType.BANK).version(0L).build())
                .category(Category.builder().id(Constants.DUMMY_CATEGORY_ID).build())
                .build();
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

    private OffsetDateTime safeParseDateTime(String dateTimeStr) {
        try {
            // Implementation depends on your parser's output format
            return OffsetDateTime.parse(dateTimeStr);
        } catch (Exception e) {
            return OffsetDateTime.now(); // Fallback to current time if parsing fails
        }
    }
}
