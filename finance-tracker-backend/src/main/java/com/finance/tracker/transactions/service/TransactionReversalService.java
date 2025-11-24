package com.finance.tracker.transactions.service;

import com.finance.tracker.transactions.domain.UpdateTransactionRequest;
import com.finance.tracker.transactions.domain.entities.Transaction;

public interface TransactionReversalService {

    String deleteTransaction(Transaction transaction);

    Transaction updateTransaction(String transactionId, UpdateTransactionRequest request);
}
