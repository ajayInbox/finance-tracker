package com.finance.tracker.sync.exceptions;

public class ScanNotFoundException extends RuntimeException {
    public ScanNotFoundException(String message) {
        super(message);
    }
}
