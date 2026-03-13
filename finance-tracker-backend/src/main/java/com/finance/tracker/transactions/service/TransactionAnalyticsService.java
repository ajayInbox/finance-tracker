package com.finance.tracker.transactions.service;

import com.finance.tracker.transactions.domain.*;

import java.util.UUID;

public interface TransactionAnalyticsService {
    TransactionsAverage search(SearchRequest searchRequest);
    MonthlyExpenseResponse getExpenseReport(UUID userId, ExpenseReportRequest request);
}
