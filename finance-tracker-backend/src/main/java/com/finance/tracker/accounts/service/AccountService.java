package com.finance.tracker.accounts.service;

import com.finance.tracker.accounts.domain.AccountCreateUpdateRequest;
import com.finance.tracker.accounts.domain.BalanceUpdateRequest;
import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.transactions.domain.entities.Transaction;

import java.util.List;

public interface AccountService {

    Account getAccountByIdAndUser(String accountId, String userId);

    void updateBalanceForTransaction(BalanceUpdateRequest request);

    Account createAccount(AccountCreateUpdateRequest request);

    List<Account> getAccounts();

    String getAccountByLastFour(String lastFour);

    void updateAccountBalance(Transaction accountId, double delta);
}
