package com.finance.tracker.sync.exceptions;

public class InvalidScanStateException extends RuntimeException {
    public InvalidScanStateException(String message) {
        super(message);
    }
}
