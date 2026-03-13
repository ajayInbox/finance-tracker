package com.finance.tracker.transactions.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class MonthlyExpenseResponse {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String currency;
    private BigDecimal total;
    private List<CategoryBreakdown> byCategory;

}
