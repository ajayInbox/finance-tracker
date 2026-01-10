package com.finance.tracker.transactions.exceptions;

public class BankNameNotExtractedException extends RuntimeException{

    public BankNameNotExtractedException(String message){
        super(message);
    }
}
