package com.finance.tracker.accounts.service;

import com.finance.tracker.accounts.domain.AccountCreateUpdateRequest;
import com.finance.tracker.accounts.domain.BalanceUpdateRequest;
import com.finance.tracker.accounts.domain.NetworthSummary;
import com.finance.tracker.accounts.domain.dto.AccountResponse;
import com.finance.tracker.accounts.domain.entities.Account;
import java.util.List;
import java.util.UUID;

public interface AccountService {

    Account getAccountByIdAndUser(UUID accountId, UUID userId);

    void updateBalanceForTransaction(BalanceUpdateRequest request, UUID userId);

    Account create(UUID userId, AccountCreateUpdateRequest req);

    Account update(UUID userId, UUID id, AccountCreateUpdateRequest req);

    List<Account> getAccounts(UUID userId);

    NetworthSummary getNetWorth(UUID userId);

    void deleteAccount(UUID userId, UUID accountId);
}
