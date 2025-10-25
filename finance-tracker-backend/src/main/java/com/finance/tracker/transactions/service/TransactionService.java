package com.finance.tracker.transactions.service;

import com.finance.tracker.transactions.domain.*;
import com.finance.tracker.transactions.domain.entities.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TransactionService {
    Transaction createNewTransaction(TransactionCreateUpdateRequest request);

    Optional<Transaction> getTransaction(String id);

    Page<Transaction> getTransactions(Pageable pageable);

    Page<TransactionsWithCategoryAndAccount> getTransactionsV2(Pageable pageable);

    TransactionsAverage search(SearchRequest searchRequest);

    MonthlyExpenseResponse getExpenseReport(String duration);
}
