package com.finance.tracker.accounts.service;

import com.finance.tracker.accounts.domain.AccountCreateUpdateRequest;
import com.finance.tracker.accounts.domain.BalanceUpdateRequest;
import com.finance.tracker.accounts.domain.entities.Account;

public interface AccountService {

    Account getAccountByIdAndUser(String accountId, String userId);

    void updateBalanceForTransaction(BalanceUpdateRequest request);

    Account createAccount(AccountCreateUpdateRequest request);
}
