package com.finance.tracker.transactions.service;

import com.finance.tracker.transactions.domain.CreateTransactionRequest;
import com.finance.tracker.transactions.domain.UpdateTransactionRequest;
import com.finance.tracker.transactions.domain.ValidationRequest;
import com.finance.tracker.transactions.domain.dtos.CreateTransactionRequestDto;

public interface TransactionValidationService {
    void validate(ValidationRequest request);
    void validate(UpdateTransactionRequest request);
}
