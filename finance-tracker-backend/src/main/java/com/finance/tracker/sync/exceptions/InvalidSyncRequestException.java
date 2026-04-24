package com.finance.tracker.sync.exceptions;

public class InvalidSyncRequestException extends RuntimeException {
    public InvalidSyncRequestException(String message) {
        super(message);
    }
}
