package com.finance.tracker.transactions.domain;

import lombok.Getter;

@Getter
public enum TransactionType {
    INCOME("income"),
    EXPENSE("expense"),
    UNKNOWN("unknown");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    // Option A: case-sensitive lookup
    public static TransactionType fromValue(String value) {
        for (TransactionType t : values()) {
            if (t.value.equals(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }

    // Option B: case-insensitive and null-safe lookup
    public static TransactionType fromValueIgnoreCase(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }
        for (TransactionType t : values()) {
            if (t.value.equalsIgnoreCase(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}

