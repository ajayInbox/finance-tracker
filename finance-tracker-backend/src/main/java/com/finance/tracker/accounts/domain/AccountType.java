package com.finance.tracker.accounts.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AccountType {

    // Assets
    BANK(true),
    SAVINGS(true),
    CHECKING(true),
    CASH(true),
    WALLET(true),
    INVESTMENT(true),
    UNKNOWN(true),

    // Liabilities
    CREDIT_CARD(false),
    LOAN(false);

    private final boolean isAsset;

    AccountType(boolean isAsset) {
        this.isAsset = isAsset;
    }

    public boolean isAssetType() {
        return isAsset;
    }

    public boolean isLiabilityType() {
        return !isAsset;
    }

    @JsonValue
    public String toJson() {
        return name();
    }

    @JsonCreator
    public static AccountType fromString(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        String normalized = value.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        for (AccountType type : values()) {
            if (type.name().equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}