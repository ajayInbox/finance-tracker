package com.finance.tracker.accounts.exceptions;

import com.finance.tracker.category.exceptions.BaseException;

public class DuplicateLastFourException extends BaseException {

    public DuplicateLastFourException(String message){
        super(message);
    }
}
