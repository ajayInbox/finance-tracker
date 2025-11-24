package com.finance.tracker.transactions.domain;

import lombok.Getter;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum TransactionType {
    INCOME("income"),
    EXPENSE("expense"),
    UNKNOWN("unknown");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    // Cached map for fast lookup
    private static final Map<String, TransactionType> LOOKUP =
            Stream.of(values())
                    .collect(Collectors.toMap(
                            t -> t.value.toLowerCase(),
                            t -> t
                    ));

    /**
     * Case-insensitive, null-safe lookup.
     * Returns UNKNOWN if value is invalid.
     */
    public static TransactionType fromValueIgnoreCase(String value) {
        if (value == null) return UNKNOWN;
        return LOOKUP.getOrDefault(value.toLowerCase(), UNKNOWN);
    }

    /**
     * Strict lookup — throws on invalid value.
     */
    public static TransactionType fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }
        TransactionType t = LOOKUP.get(value.toLowerCase());
        if (t == null || t == UNKNOWN) {
            throw new IllegalArgumentException("Unknown value: " + value);
        }
        return t;
    }
}
