package com.finance.tracker.sync.exceptions;

public class ScanAccessDeniedException extends RuntimeException {
    public ScanAccessDeniedException(String message) {
        super(message);
    }
}
