package com.finance.tracker.accounts.exceptions;

import com.finance.tracker.category.exceptions.BaseException;

public class AmountGtCurrentBalance extends BaseException {

    public AmountGtCurrentBalance(String message){
        super(message);
    }
}
