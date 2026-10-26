package com.finance.tracker.transactions.service;

import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.accounts.service.AccountService;
import com.finance.tracker.category.domain.CategoryType;
import com.finance.tracker.category.domain.entities.Category;
import com.finance.tracker.category.repository.CategoryRepository;
import com.finance.tracker.category.service.CategoryService;
import com.finance.tracker.sync.domain.EndScanRequest;
import com.finance.tracker.sync.domain.dtos.ScanStartResponse;
import com.finance.tracker.sync.exceptions.InvalidSyncRequestException;
import com.finance.tracker.sync.service.SyncService;
import com.finance.tracker.transactions.domain.*;
import com.finance.tracker.transactions.domain.Currency;
import com.finance.tracker.transactions.domain.dtos.BatchUpdateTransactionRequestDto;
import com.finance.tracker.transactions.domain.entities.Transaction;
import com.finance.tracker.transactions.repository.TransactionBatchRepository;
import com.finance.tracker.transactions.repository.TransactionRepository;
import com.finance.tracker.transactions.utilities.DateParserUtils;
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
    private final CategoryRepository categoryRepository;
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
                Transaction trnx = newEntity(req, userId);
                Category category = categoryService.validateAndGet(userId, req.categoryId(), CategoryType.fromValueIgnoreCase(req.type()));
                trnx.setCategory(category);
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

            // Step 4: Update balance only if an account was linked
            for (Transaction txn : updatedTransactions) {
                if (txn.getAccountId() != null) {
                    accountService.updateBalance(txn.getAccountId(),
                            userId,
                            txn.getId(),
                            txn.getType(),
                            txn.getAmount());
                }
            }

            return null;
        });
    }

    @Transactional
    public BatchSyncResponse processBatch(UUID userId, BatchSyncRequest request) {
        if (request == null) {
            throw new InvalidSyncRequestException("Batch request is required");
        }
        if (request.smsList() == null) {
            throw new InvalidSyncRequestException("smsList is required");
        }

        ScanStartResponse startScan = syncService.startScan(userId, request.fromTimestamp());

        // 1. Bulk Deduplication scoped to this user
        Map<String, SmsRequest> incomingMap = request.smsList().stream()
                .collect(Collectors.toMap(this::generateSecureId, m -> m, (a, b) -> a));

        Set<String> existingIds = transactionRepository.findExistingIdentifiers(userId, incomingMap.keySet());

        List<SmsRequest> newMessages = incomingMap.entrySet().stream()
                .filter(entry -> !existingIds.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();

        int duplicates = request.smsList().size() - newMessages.size();

        // 2. Parallel Processing with Virtual Threads
        List<Transaction> transactionsToSave = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger failedToParse = new AtomicInteger(0);

        List<CompletableFuture<Transaction>> futures = newMessages.stream()
                .map(msg -> CompletableFuture.supplyAsync(() -> {
                    String uid = generateSecureId(msg);
                    return smsService.parseTransactionFromSms(msg)
                            .map(parsed -> getDraftTransaction(userId, uid, parsed, msg, startScan.scanId()))
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
            transactionRepository.saveAll(transactionsToSave);
        }

        long latestTimestamp = request.smsList().stream()
                .mapToLong(SmsRequest::getTimestamp)
                .max()
                .orElse(0L);

        // 4. Automatic End Scan: Update History
        EndScanRequest endScanRequest = EndScanRequest.builder()
                .transactionsCreated(transactionsToSave.size())
                .duplicatesSkipped(duplicates)
                .totalSmsProcessed(request.smsList().size())
                .failedToParse(failedToParse.get())
                .build();

        syncService.finalizeScan(
                userId, startScan.scanId(),
                latestTimestamp,
                endScanRequest
        );

        // 5. Update Metadata: Move watermark forward
        if (latestTimestamp > 0) {
            syncService.updateMetadata(userId, latestTimestamp);
        }

        log.info("Scan {} completed: {} new txns", startScan.scanId(), transactionsToSave.size());

        return new BatchSyncResponse(
                transactionsToSave.size(),
                duplicates,
                failedToParse.get()
        );
    }

    private String generateSecureId(SmsRequest message) {
        return DigestUtils.md5DigestAsHex((message.getBody() + message.getTimestamp()).getBytes());
    }

    private Transaction getDraftTransaction(UUID userId, String uid, ParsedTransaction pt, SmsRequest msg, UUID scanId) {
        BigDecimal amount = new BigDecimal(pt.getAmount().replace(",", ""));

        TransactionType txnType = pt.getType() != null ? pt.getType() : TransactionType.EXPENSE;
        OffsetDateTime occurredAt = DateParserUtils.parseFlexible(pt.getDateTime(), msg.getTimestamp());

        String merchant = pt.getMerchant() != null && !pt.getMerchant().isBlank()
                ? pt.getMerchant()
                : "Unknown Merchant";
        String txnName = pt.getMerchant() != null && !pt.getMerchant().isBlank()
                ? pt.getMerchant()
                : "SMS Transaction";

        UUID matchedAccountId = matchAccount(userId, pt.getLastFour());
        Category defaultCategory = resolveDefaultCategory(userId, txnType);

        String notes = String.format("Bank: %s%s",
                pt.getBank() != null ? pt.getBank() : "N/A",
                pt.getReferenceId() != null ? "\nRef: " + pt.getReferenceId() : "");

        return Transaction.builder()
                .amount(amount)
                .createdAt(Instant.now())
                .currency(Currency.INR)
                .transactionName(txnName)
                .status(TransactionStatus.DRAFT)
                .lastAction("CREATED")
                .userId(userId)
                .type(txnType)
                .occurredAt(occurredAt)
                .postedAt(OffsetDateTime.now())
                .merchant(merchant)
                .source(TransactionSource.SMS)
                .notes(notes)
                .uniqueIdentifier(uid)
                .originalMessage(msg.getBody())
                .externalRef(String.format("Scan Id: %s", scanId))
                .accountId(matchedAccountId)
                .category(defaultCategory)
                .build();
    }

    private UUID matchAccount(UUID userId, String lastFour) {
        if (lastFour == null || lastFour.isBlank()) {
            return null;
        }
        try {
            List<Account> accounts = accountService.getAccounts(userId);
            if (accounts == null || accounts.isEmpty()) {
                return null;
            }
            String cleanDigits = lastFour.replaceAll("[^0-9]", "");
            if (cleanDigits.length() > 4) {
                cleanDigits = cleanDigits.substring(cleanDigits.length() - 4);
            }
            if (cleanDigits.isEmpty()) {
                return null;
            }
            final String targetDigits = cleanDigits;
            return accounts.stream()
                    .filter(a -> a.getLastFour() != null && a.getLastFour().endsWith(targetDigits))
                    .map(Account::getId)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Failed matching account for lastFour: {}", lastFour, e);
            return null;
        }
    }

    private Category resolveDefaultCategory(UUID userId, TransactionType type) {
        String categoryTypeStr = (type == TransactionType.INCOME) ? "INCOME" : "EXPENSE";
        List<Category> userCategories = categoryRepository.findAllByUserIdAndDeletedAtIsNullAndIsActiveTrue(userId);
        return userCategories.stream()
                .filter(c -> c.getType() != null && c.getType().name().equalsIgnoreCase(categoryTypeStr))
                .filter(c -> c.getName().equalsIgnoreCase("Other") || c.getName().equalsIgnoreCase("Uncategorized") || c.getName().equalsIgnoreCase("General"))
                .findFirst()
                .orElseGet(() -> userCategories.stream()
                        .filter(c -> c.getType() != null && c.getType().name().equalsIgnoreCase(categoryTypeStr))
                        .findFirst()
                        .orElseGet(() -> userCategories.stream().findFirst().orElseGet(this::dummyCategory)));
    }

    private Category dummyCategory() {
        return Category.builder()
                .id(UUID.fromString("dc9ab23d-5b9f-41c7-b9fb-d043e0e3a835"))
                .name("General")
                .description("Default Category")
                .type(CategoryType.EXPENSE)
                .build();
    }

    private Transaction newEntity(BatchUpdateTransactionRequestDto req, UUID userId) {
        return Transaction.builder()
                .id(req.id())
                .userId(userId)
                .transactionName(req.transactionName())
                .notes(req.notes())
                .amount(req.amount())
                .accountId(req.accountId())
                .merchant(req.merchant())
                .type(TransactionType.fromValueIgnoreCase(req.type()))
                .occurredAt(OffsetDateTime.of(req.occurredAt(), ZoneOffset.UTC))
                .tags(req.tags())
                .currency(Currency.valueOf(req.currency()))
                .status(TransactionStatus.CONFIRMED)
                .build();
    }

    @Transactional
    public void deleteDraftTransactions(List<UUID> transactionIds) {
        if (transactionIds == null || transactionIds.isEmpty()) {
            return;
        }
        transactionRepository.deleteByIdIn(transactionIds);
    }

    @Transactional
    public void deleteDraftTransactions(UUID userId, List<UUID> transactionIds) {
        if (transactionIds == null || transactionIds.isEmpty()) {
            return;
        }
        transactionRepository.deleteDraftsByUserIdAndIds(userId, transactionIds);
    }
}
