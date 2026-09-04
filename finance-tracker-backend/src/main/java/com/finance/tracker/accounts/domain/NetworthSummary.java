package com.finance.tracker.accounts.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("totalAssets")
    public BigDecimal getTotalAssets() {
        return assets != null && assets.getTotal() != null ? assets.getTotal() : BigDecimal.ZERO;
    }

    @JsonProperty("totalLiabilities")
    public BigDecimal getTotalLiabilities() {
        return liabilities != null && liabilities.getTotal() != null ? liabilities.getTotal() : BigDecimal.ZERO;
    }

    @JsonProperty("currency")
    public String getCurrency() {
        return "INR";
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValueNumber {

        private BigDecimal total;
        private int number;

    }
}


