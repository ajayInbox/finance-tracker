package com.finance.tracker.transactions.exceptions;

public class SmsParsingFailedException extends RuntimeException{

    public SmsParsingFailedException(String sms) {
        super("Failed to parse SMS, " + mask(sms));
    }

    private static String mask(String sms) {
        return sms.replaceAll("\\d{12,}", "****");
    }

}
