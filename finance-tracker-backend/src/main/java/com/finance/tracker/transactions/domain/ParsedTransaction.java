package com.finance.tracker.transactions.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParsedTransaction {

    private String bank;
    private String amount;
    private String merchant;
    private String lastFour;
    private String dateTime;
    private String availableLimit;
    private String referenceId;
    private double confidence;

}
