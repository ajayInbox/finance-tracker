package com.finance.tracker.transactions.service.impl;

import com.finance.tracker.accounts.domain.AccountType;
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
import com.finance.tracker.transactions.utilities.DateParserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.finance.tracker.accounts.repository.AccountRepository;
import com.finance.tracker.category.repository.CategoryRepository;

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
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    @Override
    public TransactionResponseDto create(CreateTransactionRequestDto request, UUID userId) {

        Category category = categoryService.validateAndGet(userId, request.categoryId(), CategoryType.fromValueIgnoreCase(request.type()));

        log.debug("Request Body for Create: {}", request);

        Transaction transaction = Transaction.builder()
                .transactionName(request.transactionName())
                .amount(request.amount())
                .type(TransactionType.fromValueIgnoreCase(request.type()))
                .currency(Currency.fromValueIgnoreCase(request.currency()))
                .category(category)
                .accountId(request.accountId())
                .userId(userId)
                .occurredAt(OffsetDateTime.of(request.occurredAt(), ZoneOffset.UTC))
                .merchant(request.merchant())
                .notes(request.notes())
                .tags(request.tags())
                .status(TransactionStatus.CONFIRMED)
                .source(TransactionSource.Manual)
                .build();
        Transaction saved = transactionRepository.saveAndFlush(transaction);
        if(request.accountId()!=null){
            accountService.updateBalance(request.accountId(),
                    userId,
                    saved.getId(),
                    saved.getType(),
                    saved.getAmount());
        }

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
    public MonthlyExpenseResponse getExpenseReport(UUID userId, ExpenseReportRequest request) {
        return analyticsService.getExpenseReport(userId, request);
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
        if (transaction.getAccountId() != null) {
            Transaction reversal = buildReversalTransaction(transaction);
            Transaction savedReversal = transactionRepository.save(reversal);

            // 3. Update Account Balance
            // It's vital this happens inside the transaction
            accountService.updateBalance(savedReversal.getAccountId(),
                    userId,
                    savedReversal.getId(),
                    savedReversal.getType(),
                    savedReversal.getAmount());
        }

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
        boolean accountChanged = (original.getAccountId() == null && request.accountId() != null) ||
                (original.getAccountId() != null && !original.getAccountId().equals(request.accountId()));
        boolean categoryChanged = !original.getCategory().getId().equals(request.categoryId());

        if (amountChanged || typeChanged || accountChanged || categoryChanged) {

            // Step A: Neutralize the old impact
            if (original.getAccountId() != null) {
                Transaction reversal = buildReversalTransaction(original);
                transactionRepository.save(reversal);
                accountService.updateBalance(reversal.getAccountId(),
                        userId,
                        reversal.getId(),
                        reversal.getType(),
                        reversal.getAmount());
            }

            // Step B: Resolve the Category and Account for the NEW transaction
            Category category = categoryService.validateAndGet(userId, request.categoryId(),
                    CategoryType.fromValueIgnoreCase(request.type()));

            // Step C: Create the "corrected" transaction
            Transaction newTxn = Transaction.builder()
                    .transactionName(request.transactionName())
                    .amount(request.amount())
                    .type(TransactionType.valueOf(request.type().toUpperCase()))
                    .currency(Currency.valueOf(request.currency()))
                    .category(category)
                    .accountId(request.accountId())
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
            if (savedNewTxn.getAccountId() != null) {
                accountService.updateBalance(savedNewTxn.getAccountId(),
                        userId,
                        savedNewTxn.getId(),
                        savedNewTxn.getType(),
                        savedNewTxn.getAmount());
            }

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
    public ParsedTxnResponse parse(UUID userId, SmsRequest message) {
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
        saveParsedTransaction(userId, uniqueIdentifier, parsedTransaction.get(), message.getBody(), message.getTimestamp());
        return ParsedTxnResponse.builder()
                .status("CREATED")
                .uniqueIdentifier(uniqueIdentifier)
                .parsedTransaction(null)
                .build();
    }

    @Override
    public List<TransactionResponseDto> getAll(UUID userId, TransactionStatus status, Pageable pageable) {
        Page<TransactionDraftProjection> res = transactionRepository.findAllTransactions(userId, status.name(), pageable);
        if(res.getContent().isEmpty()) {return List.of();}
        return res.map(this::mapToResponseDto)
                .toList();
    }

    private void saveParsedTransaction(UUID userId, String uniqueIdentifier, ParsedTransaction parsedTransaction, String originalMessage, Long smsTimestamp) {
        // 1. Sanitize the amount string to handle commas
        String sanitizedAmount = parsedTransaction.getAmount().replace(",", "");

        // 2. Build detailed notes for the Review Page
        String notes = String.format("Bank: %s\nRef: %s",
                parsedTransaction.getBank(),
                parsedTransaction.getReferenceId() != null ? parsedTransaction.getReferenceId() : "N/A");

        TransactionType txnType = parsedTransaction.getType() != null
                ? parsedTransaction.getType()
                : TransactionType.EXPENSE;

        OffsetDateTime occurredAt = DateParserUtils.parseFlexible(parsedTransaction.getDateTime(), smsTimestamp);

        String merchant = parsedTransaction.getMerchant() != null ? parsedTransaction.getMerchant() : "Unknown Merchant";
        String txnName = parsedTransaction.getMerchant() != null ? parsedTransaction.getMerchant() : "SMS Transaction";

        // 3. Map the DRAFT entity
        Transaction draftTransaction = Transaction.builder()
                .amount(new BigDecimal(sanitizedAmount))
                .createdAt(Instant.now())
                .currency(Currency.INR)
                .transactionName(txnName)
                .status(TransactionStatus.DRAFT)
                .lastAction("CREATED")
                .accountId(null)
                .category(resolveDefaultCategory(userId, txnType))
                .userId(userId)
                .type(txnType)
                .occurredAt(occurredAt)
                .postedAt(OffsetDateTime.now())
                .merchant(merchant)
                .source(TransactionSource.SMS)
                .notes(notes)
                .uniqueIdentifier(uniqueIdentifier)
                .originalMessage(originalMessage)
                .build();

        transactionRepository.save(draftTransaction);
    }

    private String generateSecureId(SmsRequest msg) {
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

    @Override
    public TransactionResponseDto mapToResponseDto(Transaction txn) {
        return TransactionResponseDto.builder()
                .id(txn.getId())
                .transactionName(txn.getTransactionName())
                .amount(txn.getAmount())
                .type(txn.getType() != null ? txn.getType().name() : TransactionType.EXPENSE.name())
                .accountId(txn.getAccountId())
                .categoryId(txn.getCategory() != null ? txn.getCategory().getId() : null)
                .categoryName(txn.getCategory() != null ? txn.getCategory().getName() : null)
                .occurredAt(txn.getOccurredAt() != null ? txn.getOccurredAt().toLocalDateTime() : LocalDateTime.now())
                .tags(txn.getTags())
                .status(txn.getStatus() != null ? txn.getStatus().name() : null)
                .merchant(txn.getMerchant())
                .notes(txn.getNotes())
                .build();
    }

    private TransactionResponseDto mapToResponseDto(TransactionDraftProjection txn) {
        return TransactionResponseDto.builder()
                .id(txn.getId())
                .transactionName(txn.getTransactionName())
                .amount(txn.getAmount())
                .type(txn.getType() != null ? txn.getType().name() : TransactionType.EXPENSE.name())
                .accountId(txn.getAccountId())
                .accountName(txn.getAccountName())
                .categoryId(txn.getCategoryId())
                .categoryName(txn.getCategoryName())
                .occurredAt(txn.getOccurredAt() != null ? txn.getOccurredAt().atOffset(ZoneOffset.UTC).toLocalDateTime() : LocalDateTime.now())
                .tags(txn.getTags())
                .status(txn.getStatus() != null ? txn.getStatus().name() : TransactionStatus.DRAFT.name())
                .originalMessage(txn.getOriginalMessage())
                .merchant(txn.getMerchant())
                .notes(txn.getNotes())
                .build();
    }

    private Transaction buildReversalTransaction(Transaction original) {
        return Transaction.builder()
                .transactionName("Reversal: " + original.getTransactionName())
                .notes("System generated reversal for transaction ID: " + original.getId())
                .amount(original.getAmount())
                .type(getReversalType(original.getType()))
                .currency(original.getCurrency())
                .category(original.getCategory())
                .userId(original.getUserId())
                .occurredAt(OffsetDateTime.now())
                .status(TransactionStatus.REVERSAL)
                .reversalOf(original)
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

    private Category dummyCategory(){
        return Category.builder()
                .id(UUID.fromString("dc9ab23d-5b9f-41c7-b9fb-d043e0e3a835"))
                .name("General")
                .description("Default Category")
                .type(CategoryType.EXPENSE)
                .build();
    }
}
