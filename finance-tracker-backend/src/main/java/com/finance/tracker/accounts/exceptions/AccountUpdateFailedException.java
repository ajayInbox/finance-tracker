package com.finance.tracker.accounts.exceptions;

import com.finance.tracker.category.exceptions.BaseException;

public class AccountUpdateFailedException extends BaseException {
    public AccountUpdateFailedException(String message) {
        super(message);
    }
}
