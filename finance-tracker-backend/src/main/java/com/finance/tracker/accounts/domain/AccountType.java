package com.finance.tracker.accounts.domain;

import lombok.Getter;

@Getter
public enum AccountType {

    // Assets
    BANK(true),
    CASH(true),
    INVESTMENT(true),

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
}