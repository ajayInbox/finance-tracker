package com.finance.tracker.transactions.service.impl;

import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.accounts.service.AccountService;
import com.finance.tracker.category.domain.CategoryType;
import com.finance.tracker.category.service.CategoryService;
import com.finance.tracker.transactions.domain.CreateTransactionRequest;
import com.finance.tracker.transactions.domain.TransactionType;
import com.finance.tracker.transactions.domain.UpdateTransactionRequest;
import com.finance.tracker.transactions.domain.ValidationRequest;
import com.finance.tracker.transactions.domain.dtos.CreateTransactionRequestDto;
import com.finance.tracker.transactions.exceptions.CurrencyMismatchException;
import com.finance.tracker.transactions.service.TransactionValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionValidationServiceImpl implements TransactionValidationService {

    private final AccountService accountService;
    private final CategoryService categoryService;

    @Override
    public void validate(ValidationRequest request) {
        validateAccountAndCategory(request);
    }

    @Override
    public void validate(UpdateTransactionRequest request) {
        //validateAccountAndCategory(request.getAccount(), request.getCategory(), request.getCurrency(), request.getType());
    }

    private void validateAccountAndCategory(ValidationRequest request) {

        Account account = accountService.getAccountByIdAndUser(request.getAccountId(), request.getUserId());
        if (!account.getCurrency().name().equalsIgnoreCase(request.getCurrency().toString())) {
            throw new CurrencyMismatchException();
        }

        categoryService.validateAndGet(
                request.getUserId(),
                request.getCategoryId(),
                CategoryType.fromValueIgnoreCase(request.getType())
        );
    }
}
