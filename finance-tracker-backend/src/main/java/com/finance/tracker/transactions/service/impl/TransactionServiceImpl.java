package com.finance.tracker.transactions.service.impl;

import com.finance.tracker.accounts.domain.BalanceUpdateRequest;
import com.finance.tracker.accounts.service.AccountService;
import com.finance.tracker.transactions.domain.*;
import com.finance.tracker.transactions.domain.entities.Transaction;
import com.finance.tracker.transactions.domain.entities.UnparsedSmsLog;
import com.finance.tracker.transactions.exceptions.SmsNotParsedException;
import com.finance.tracker.transactions.mapper.TransactionMapper;
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
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionValidationService validationService;
    private final TransactionReversalService reversalService;
    private final TransactionSmsService smsService;
    private final TransactionAnalyticsService analyticsService;
    private final AccountService accountService;
    private final TransactionMapper transactionMapper;
    private final UnparsedSmsLogsRepository unparsedSmsLogsRepository;

    @Transactional
    @Override
    public Transaction createNewTransaction(CreateTransactionRequest request) {
        validationService.validate(request);
        // TODO: actual user id
        String userId = null;
        log.debug("Request Body: {}", request);

        Transaction transaction = transactionMapper.toNewEntity(request, userId);
        Transaction saved = transactionRepository.save(transaction);
        updateBalanceFor(saved);

        return saved;
    }

    @Override
    public Optional<Transaction> getTransaction(String id) {
        return transactionRepository.findById(id);
    }

    @Override
    public Page<Transaction> getTransactions(Pageable pageable) {
        return transactionRepository.findAllTransactions(pageable);
    }

    @Override
    public Page<TransactionsWithCategoryAndAccount> getTransactionsV2(Pageable pageable) {
        return transactionRepository.fetchTransactions(pageable);
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
    public String deleteTransaction(Transaction transaction) {
        return reversalService.deleteTransaction(transaction);
    }

    @Override
    public Transaction updateTransaction(String transactionId, UpdateTransactionRequest request) {
        return reversalService.updateTransaction(transactionId, request);
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
                .lastAction(LastAction.CREATED)
                // Ensure occurredAt is parsed using a consistent formatter or fallback to now
                .occurredAt(safeParseDateTime(parsedTransaction.getDateTime()))
                .postedAt(Instant.now())
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

    private Instant safeParseDateTime(String dateTimeStr) {
        try {
            // Implementation depends on your parser's output format
            return Instant.parse(dateTimeStr);
        } catch (Exception e) {
            return Instant.now(); // Fallback to current time if parsing fails
        }
    }
}
