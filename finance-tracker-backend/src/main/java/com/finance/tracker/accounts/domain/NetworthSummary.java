package com.finance.tracker.accounts.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NetworthSummary {

    private ValueNumber assets;
    private ValueNumber liabilities;
    private BigDecimal netWorth;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValueNumber {

        private BigDecimal total;
        private int number;

    }


}

