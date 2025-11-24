package com.finance.tracker.transactions.service.impl;

import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.accounts.service.AccountService;
import com.finance.tracker.category.service.CategoryService;
import com.finance.tracker.transactions.domain.CreateTransactionRequest;
import com.finance.tracker.transactions.domain.TransactionType;
import com.finance.tracker.transactions.domain.UpdateTransactionRequest;
import com.finance.tracker.transactions.exceptions.CurrencyMismatchException;
import com.finance.tracker.transactions.service.TransactionValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionValidationServiceImpl implements TransactionValidationService {

    private final AccountService accountService;
    private final CategoryService categoryService;

    @Override
    public void validate(CreateTransactionRequest request) {
        // TODO: wire actual user id later
        String userId = null;

        Account account = accountService.getAccountByIdAndUser(request.getAccount(), userId);
        if (!account.getCurrency().equalsIgnoreCase(request.getCurrency())) {
            throw new CurrencyMismatchException();
        }

        categoryService.validateCategoryForTransaction(
                userId,
                request.getCategory(),
                TransactionType.fromValueIgnoreCase(request.getType())
        );
    }

    @Override
    public void validate(UpdateTransactionRequest request) {

    }
}
