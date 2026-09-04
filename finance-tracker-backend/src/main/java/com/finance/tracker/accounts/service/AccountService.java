package com.finance.tracker.accounts.service;

import com.finance.tracker.accounts.domain.AccountCreateUpdateRequest;
import com.finance.tracker.accounts.domain.BalanceUpdateRequest;
import com.finance.tracker.accounts.domain.NetworthSummary;
import com.finance.tracker.accounts.domain.dto.AccountResponse;
import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.transactions.domain.TransactionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService {

    void updateBalance(UUID accountId, UUID userId, UUID transactionId, TransactionType type, BigDecimal amount);

    Account getAccountByIdAndUser(UUID accountId, UUID userId);

    void updateBalanceForTransaction(BalanceUpdateRequest request, UUID userId);

    Account create(UUID userId, AccountCreateUpdateRequest req);

    Account update(UUID userId, UUID id, AccountCreateUpdateRequest req);

    List<Account> getAccounts(UUID userId);

    NetworthSummary getNetWorth(UUID userId);

    Account initializeDefaults(UUID userId);

    void deleteAccount(UUID userId, UUID accountId);
}
