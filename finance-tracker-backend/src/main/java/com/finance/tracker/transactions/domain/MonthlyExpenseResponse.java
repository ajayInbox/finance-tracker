package com.finance.tracker.transactions.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class MonthlyExpenseResponse {

    private String month;
    private String currency;
    private BigDecimal total;
    private List<CategoryBreakdown> byCategory;

}
