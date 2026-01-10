package com.finance.tracker.transactions.service.impl;

import com.finance.tracker.accounts.domain.BalanceUpdateRequest;
import com.finance.tracker.accounts.service.AccountService;
import com.finance.tracker.transactions.domain.*;
import com.finance.tracker.transactions.domain.entities.Transaction;
import com.finance.tracker.transactions.exceptions.SmsNotParsedException;
import com.finance.tracker.transactions.mapper.TransactionMapper;
import com.finance.tracker.transactions.repository.TransactionRepository;
import com.finance.tracker.transactions.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void exportMessages(List<SmsMessage> messageList) {
        smsService.exportMessages(messageList);
    }

    @Override
    public void exportMessagesSendToQueue(List<SmsMessage> messageList) {
        smsService.exportMessagesSendToQueue(messageList);
    }

    @Override
    public void createTransactionFromQueueMsg(SmsMessage message) {

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
    public ParsedTransaction parse(SmsMessage message) {
        return smsService.parseTransactionFromSms(message)
                .orElseThrow(() -> new SmsNotParsedException("Unable to Parse"));
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
}
