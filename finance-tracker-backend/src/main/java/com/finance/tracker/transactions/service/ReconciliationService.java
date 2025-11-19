package com.finance.tracker.transactions.service;

import com.finance.tracker.transactions.domain.ReconciliationRequest;

import java.util.Map;

public interface ReconciliationService {

    void addEntry(ReconciliationRequest request);
}
