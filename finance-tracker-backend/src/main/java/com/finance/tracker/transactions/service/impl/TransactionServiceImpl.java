package com.finance.tracker.transactions.service.impl;

import com.finance.tracker.accounts.domain.BalanceUpdateRequest;
import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.accounts.service.AccountService;
import com.finance.tracker.category.domain.CategoryType;
import com.finance.tracker.category.domain.entities.Category;
import com.finance.tracker.category.service.CategoryService;
import com.finance.tracker.transactions.domain.*;
import com.finance.tracker.transactions.domain.dtos.CreateTransactionRequestDto;
import com.finance.tracker.transactions.domain.dtos.TransactionResponseDto;
import com.finance.tracker.transactions.domain.dtos.UpdateTransactionRequestDto;
import com.finance.tracker.transactions.domain.entities.Transaction;
import com.finance.tracker.transactions.domain.entities.UnparsedSmsLog;
import com.finance.tracker.transactions.exceptions.TransactionNotFoundException;
import com.finance.tracker.transactions.repository.TransactionRepository;
import com.finance.tracker.transactions.repository.UnparsedSmsLogsRepository;
import com.finance.tracker.transactions.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionSmsService smsService;
    private final TransactionAnalyticsService analyticsService;
    private final AccountService accountService;
    private final UnparsedSmsLogsRepository unparsedSmsLogsRepository;
    private final CategoryService categoryService;

    @Transactional
    @Override
    public TransactionResponseDto create(CreateTransactionRequestDto request, UUID userId) {

        Category category = categoryService.validateAndGet(userId, request.categoryId(), CategoryType.fromValueIgnoreCase(request.type()));
        Account account = accountService.getAccountByIdAndUser(request.accountId(), userId);

        log.debug("Request Body for Create: {}", request);

        Transaction transaction = Transaction.builder()
                .transactionName(request.transactionName())
                .amount(request.amount())
                .type(TransactionType.valueOf(request.type()))
                .currency(Currency.valueOf(request.currency()))
                .category(category)
                .account(account)
                .userId(userId)
                .occurredAt(OffsetDateTime.of(request.occurredAt(), ZoneOffset.UTC))
                .merchant(request.merchant())
                .notes(request.notes())
                .tags(request.tags())
                .status(TransactionStatus.CONFIRMED)
                .source(TransactionSource.Manual)
                .build();
        Transaction saved = transactionRepository.save(transaction);
        updateBalanceFor(saved, userId);

        return mapToResponseDto(saved);
    }

    @Override
    public Optional<Transaction> getTransaction(UUID id) {
        return transactionRepository.findById(id);
    }

    @Override
    public List<TransactionResponseDto> getTransactions(Pageable pageable) {
        log.debug("Request Body for Get All: {}", pageable);
        Page<Transaction> res = transactionRepository.findAll(pageable);
        if(res.getContent().isEmpty()) {return List.of();}
        return res.map(this::mapToResponseDto)
                .toList();
    }

    @Override
    public Page<TransactionsWithCategoryAndAccount> getTransactionsV2(String status, Pageable pageable) {
        return transactionRepository.fetchTransactions(status, pageable);
    }

    @Override
    public TransactionsAverage search(SearchRequest searchRequest) {
        return analyticsService.search(searchRequest);
    }

    @Override
    public MonthlyExpenseResponse getExpenseReport(ExpenseReportDuration duration) {
        if(duration==null){
            duration = ExpenseReportDuration.THIS_MONTH;
        }
        return analyticsService.getExpenseReport(duration);
    }

    @Override
    public void exportMessages(List<SmsRequest> messageList) {
        smsService.exportMessages(messageList);
    }

    @Override
    public void exportMessagesSendToQueue(List<SmsRequest> messageList) {
        smsService.exportMessagesSendToQueue(messageList);
    }

    @Override
    public void createTransactionFromQueueMsg(SmsRequest message) {

        //smsService.createTransactionFromQueueMsg(message);
    }

    @Override
    public void deleteTransaction(UUID userId, Transaction transaction) {
        // 1. Guard Clause
        if (transaction.getStatus() == TransactionStatus.DELETED ||
                transaction.getStatus() == TransactionStatus.REVERSAL) {
            throw new RuntimeException("Transaction has been deleted");
        }

        // 2. Build and Save Reversal
        // This restores the funds to the Account balance
        Transaction reversal = buildReversalTransaction(transaction);
        Transaction savedReversal = transactionRepository.save(reversal);

        // 3. Update Account Balance
        // It's vital this happens inside the transaction
        updateBalanceFor(savedReversal, userId);

        // 4. Update Original Transaction
        // We mark it DELETED so it no longer shows up in user's active history
        transaction.setLastAction("DELETED");
        transaction.setStatus(TransactionStatus.DELETED);
        transaction.setUpdatedAt(Instant.now());
        transactionRepository.save(transaction);
    }

    @Override
    public TransactionResponseDto update(UUID userId, UUID trxId, UpdateTransactionRequestDto request) {
        Transaction original = transactionRepository.findByIdAndUserId(trxId, userId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

        // 1. Check for Structural Changes
        boolean amountChanged = original.getAmount().compareTo(request.amount()) != 0;
        boolean typeChanged = !original.getType().name().equalsIgnoreCase(request.type());
        boolean accountChanged = !original.getAccount().getId().equals(request.accountId());
        boolean categoryChanged = !original.getCategory().getId().equals(request.categoryId());

        if (amountChanged || typeChanged || accountChanged || categoryChanged) {

            // Step A: Neutralize the old impact
            Transaction reversal = buildReversalTransaction(original);
            transactionRepository.save(reversal);
            updateBalanceFor(reversal, userId);

            // Step B: Resolve the Category and Account for the NEW transaction
            // We always fetch these to ensure we have the most recent validated objects
            Category category = categoryService.validateAndGet(userId, request.categoryId(),
                    CategoryType.fromValueIgnoreCase(request.type()));
            Account account = accountService.getAccountByIdAndUser(request.accountId(), userId);

            // Step C: Create the "corrected" transaction
            // This takes ALL fields from the request, including non-structural ones
            Transaction newTxn = Transaction.builder()
                    .transactionName(request.transactionName())
                    .amount(request.amount())
                    .type(TransactionType.valueOf(request.type().toUpperCase()))
                    .currency(Currency.valueOf(request.currency()))
                    .category(category)
                    .account(account)
                    .userId(userId)
                    .occurredAt(OffsetDateTime.of(request.occurredAt(), ZoneOffset.UTC))
                    .merchant(request.merchant())
                    .notes(request.notes())
                    .tags(request.tags())
                    .status(TransactionStatus.CONFIRMED)
                    .source(original.getSource()) // Keep original source (e.g., SMS)
                    .externalRef(original.getExternalRef())
                    .build();

            Transaction savedNewTxn = transactionRepository.save(newTxn);
            updateBalanceFor(savedNewTxn, userId);

            // Step D: Archive the old transaction
            original.setStatus(TransactionStatus.SUPERSEDED);
            original.setLastAction("REPLACED_BY_" + savedNewTxn.getId());
            transactionRepository.save(original);

            return mapToResponseDto(savedNewTxn);
        }

        // 4. Non-Structural Changes (In-place update)
        original.setMerchant(request.merchant());
        original.setCurrency(Currency.valueOf(request.currency()));
        original.setNotes(request.notes());
        original.setOccurredAt(OffsetDateTime.of(request.occurredAt(), ZoneOffset.UTC));
        original.setTransactionName(request.transactionName());
        original.setTags(request.tags());
        original.setLastAction("UPDATED");
        original.setUpdatedAt(Instant.now());

        return mapToResponseDto(transactionRepository.save(original));
    }

    @Override
    public ParsedTxnResponse parse(SmsRequest message) {
        String uniqueIdentifier = generateSecureId(message);
        Optional<Transaction> optionalTransaction = transactionRepository.findTransactionByUniqueIdentifier(uniqueIdentifier);
        if (optionalTransaction.isPresent()) {
            return ParsedTxnResponse.builder()
                    .status("DUPLICATE")
                    .uniqueIdentifier(uniqueIdentifier)
                    .parsedTransaction(null)
                    .build();
        }
        Optional<ParsedTransaction> parsedTransaction = smsService.parseTransactionFromSms(message);
        if(parsedTransaction.isEmpty()){
            logFailedParsing(message, "NO_REGEX_MATCH");

            return ParsedTxnResponse.builder()
                    .status("FAILED_TO_PARSE") // Mobile app knows it was received but couldn't be read
                    .uniqueIdentifier(uniqueIdentifier)
                    .parsedTransaction(null)
                    .build();
        }
        // save parsed transaction as draft
        saveParsedTransaction(uniqueIdentifier, parsedTransaction.get());
        return ParsedTxnResponse.builder()
                .status("CREATED")
                .uniqueIdentifier(uniqueIdentifier)
                .parsedTransaction(null)
                .build();
    }

    @Override
    public List<TransactionResponseDto> getAll(UUID userId, TransactionStatus status, Pageable pageable) {
        Page<Transaction> res = transactionRepository.findAllByUserIdAndStatus(userId, status, pageable);
        if(res.getContent().isEmpty()) {return List.of();}
        return res.map(this::mapToResponseDto)
                .toList();
    }

    private void updateBalanceFor(Transaction txn, UUID userId) {
        accountService.updateBalanceForTransaction(
                new BalanceUpdateRequest(
                        txn.getAccount().getId(),
                        txn.getAmount(),
                        txn.getType(),
                        txn.getId()
                ), userId
        );
    }

    private void saveParsedTransaction(String uniqueIdentifier, ParsedTransaction parsedTransaction) {
        // 1. Sanitize the amount string to handle commas
        String sanitizedAmount = parsedTransaction.getAmount().replace(",", "");

        // 2. Build detailed notes for the Review Page
        String notes = String.format("Bank: %s\nRef: %s",
                parsedTransaction.getBank(),
                parsedTransaction.getReferenceId() != null ? parsedTransaction.getReferenceId() : "N/A");

        // 3. Map the DRAFT entity
        Transaction draftTransaction = Transaction.builder()
                .amount(new BigDecimal(sanitizedAmount))
                .createdAt(Instant.now())
                .currency(Currency.INR)
                .transactionName("Auto-detected Transaction")
                .status(TransactionStatus.DRAFT)
                .lastAction("CREATED")
                // Ensure occurredAt is parsed using a consistent formatter or fallback to now
                .occurredAt(safeParseDateTime(parsedTransaction.getDateTime()))
                .postedAt(OffsetDateTime.now())
                .merchant(parsedTransaction.getMerchant() != null ? parsedTransaction.getMerchant() : "Unknown Merchant")
                .source(TransactionSource.SMS)
                .notes(notes)
                .uniqueIdentifier(uniqueIdentifier)
                .build();

        transactionRepository.save(draftTransaction);
    }

    private String generateSecureId(SmsRequest msg) {
        // For production, use DigestUtils.sha256Hex(msg.getBody())
        return String.format("sms_%s_%d_%d",
                msg.getSender(),
                msg.getTimestamp(),
                msg.getBody().hashCode());
    }

    private void logFailedParsing(SmsRequest msg, String reason) {
        UnparsedSmsLog log = new UnparsedSmsLog();
        log.setSender(msg.getSender());
        log.setSmsRawBody(msg.getBody());
        log.setTimestamp(msg.getTimestamp());
        log.setErrorReason(reason);
        unparsedSmsLogsRepository.save(log);
    }

    private OffsetDateTime safeParseDateTime(String dateTimeStr) {
        try {
            // Implementation depends on your parser's output format
            return OffsetDateTime.parse(dateTimeStr);
        } catch (Exception e) {
            return OffsetDateTime.now(); // Fallback to current time if parsing fails
        }
    }

    @Override
    public TransactionResponseDto mapToResponseDto(Transaction txn) {
        return TransactionResponseDto.builder()
                .id(txn.getId())
                .transactionName(txn.getTransactionName())
                .amount(txn.getAmount())
                .type(txn.getType().name())
                .categoryName(txn.getCategory().getName())
                .accountName(txn.getAccount().getAccountName())
                .occurredAt(txn.getOccurredAt())
                .tags(txn.getTags())
                .status(txn.getStatus().name())
                .build();
    }

    private Transaction buildReversalTransaction(Transaction original) {
        return Transaction.builder()
                // 1. Audit Details
                .transactionName("Reversal: " + original.getTransactionName())
                .notes("System generated reversal for transaction ID: " + original.getId())
                .amount(original.getAmount())
                .type(getReversalType(original.getType()))
                .currency(original.getCurrency())
                .account(original.getAccount())
                .category(original.getCategory())
                .userId(original.getUserId())

                // 4. Timestamps
                .occurredAt(OffsetDateTime.now()) // The reversal happens "now"
                .status(TransactionStatus.REVERSAL)
                .reversalOf(original) // Self-reference link
                .source(TransactionSource.Manual)
                .build();
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
