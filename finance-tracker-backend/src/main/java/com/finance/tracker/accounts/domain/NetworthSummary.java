package com.finance.tracker.accounts.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NetworthSummary {

    private ValueNumber assets;
    private ValueNumber liabilities;
    private double netWorth;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValueNumber {

        private double total;
        private int number;

    }


}

