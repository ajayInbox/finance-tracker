package com.finance.tracker.transactions.service;

import com.finance.tracker.transactions.domain.CreateTransactionRequest;
import com.finance.tracker.transactions.domain.UpdateTransactionRequest;

public interface TransactionValidationService {
    void validate(CreateTransactionRequest request);
    void validate(UpdateTransactionRequest request);
}
