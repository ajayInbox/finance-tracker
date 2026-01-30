package com.finance.tracker.transactions.service;

import com.finance.tracker.transactions.domain.*;
import com.finance.tracker.transactions.domain.entities.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TransactionService {
    Transaction createNewTransaction(CreateTransactionRequest request);

    Optional<Transaction> getTransaction(String id);

    Page<Transaction> getTransactions(Pageable pageable);

    Page<TransactionsWithCategoryAndAccount> getTransactionsV2(Pageable pageable);

    TransactionsAverage search(SearchRequest searchRequest);

    MonthlyExpenseResponse getExpenseReport(ExpenseReportDuration duration);

    void exportMessages(List<SmsRequest> messageList);

    void exportMessagesSendToQueue(List<SmsRequest> messageList);

    void createTransactionFromQueueMsg(SmsRequest message);

    String deleteTransaction(Transaction transaction);

    Transaction updateTransaction(String transactionId, UpdateTransactionRequest request);

    ParsedTxnResponse parse(SmsRequest message);
}
