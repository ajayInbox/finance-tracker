package com.finance.tracker.transactions.service;

import com.finance.tracker.transactions.domain.*;

public interface TransactionAnalyticsService {
    TransactionsAverage search(SearchRequest searchRequest);
    MonthlyExpenseResponse getExpenseReport(ExpenseReportDuration duration);
}
