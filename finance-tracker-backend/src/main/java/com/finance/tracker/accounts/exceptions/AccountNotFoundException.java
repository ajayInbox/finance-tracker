package com.finance.tracker.accounts.exceptions;

import com.finance.tracker.category.exceptions.BaseException;

public class AccountNotFoundException extends BaseException {

    public AccountNotFoundException(String message){
        super(message);
    }
}
