package com.finance.tracker.accounts.exceptions;

import com.finance.tracker.category.exceptions.BaseException;

public class AccountAmountNegativeException extends BaseException {

    public AccountAmountNegativeException(String message){
        super(message);
    }
}
