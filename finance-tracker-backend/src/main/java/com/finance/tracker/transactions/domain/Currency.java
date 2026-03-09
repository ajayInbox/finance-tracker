package com.finance.tracker.transactions.domain;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Currency {
    INR("inr");

    private final String value;

    Currency(String value) {
        this.value = value;
    }

    // Cached map for fast lookup
    private static final Map<String, Currency> LOOKUP =
            Stream.of(values())
                    .collect(Collectors.toMap(
                            t -> t.value.toLowerCase(),
                            t -> t
                    ));

    /**
     * Case-insensitive, null-safe lookup.
     * Returns UNKNOWN if value is invalid.
     */
    public static Currency fromValueIgnoreCase(String value) {
        if (value == null) return INR;
        return LOOKUP.getOrDefault(value.toLowerCase(), INR);
    }
}
