package com.finance.tracker.transactions.exceptions;

public class SmsNotParsedException extends RuntimeException{

    public SmsNotParsedException(String message){
        super(message);
    }
}
