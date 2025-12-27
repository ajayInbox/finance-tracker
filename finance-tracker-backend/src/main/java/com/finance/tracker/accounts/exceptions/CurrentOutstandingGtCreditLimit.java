package com.finance.tracker.accounts.exceptions;

import com.finance.tracker.category.exceptions.BaseException;

public class CurrentOutstandingGtCreditLimit extends BaseException {

    public CurrentOutstandingGtCreditLimit(String message){
        super(message);
    }
}
