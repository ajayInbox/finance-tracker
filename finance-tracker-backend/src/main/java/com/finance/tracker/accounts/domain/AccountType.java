package com.finance.tracker.accounts.domain;

import lombok.Getter;

@Getter
public enum AccountType {

    BANK("bank"),
    CARD("card"),
    WALLET("wallet"),
    CASH("cash");

    private final String value;

    AccountType(String value) {
        this.value = value;
    }

    // Convert string to enum safely
    public static AccountType fromString(String value) {
        for (AccountType type : AccountType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid account type: " + value);
    }
}