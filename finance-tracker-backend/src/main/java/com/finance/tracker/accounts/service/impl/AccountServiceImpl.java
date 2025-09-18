package com.finance.tracker.accounts.service.impl;

import com.finance.tracker.accounts.domain.AccountCreateUpdateRequest;
import com.finance.tracker.accounts.domain.AccountType;
import com.finance.tracker.accounts.domain.BalanceUpdateRequest;
import com.finance.tracker.accounts.domain.SnapshotCreateRequest;
import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.accounts.exceptions.AccountNotFoundException;
import com.finance.tracker.accounts.repository.AccountRepository;
import com.finance.tracker.accounts.service.AccountService;
import com.finance.tracker.accounts.service.AccountTransactionSnapshotService;
import com.finance.tracker.transactions.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountTransactionSnapshotService accountTransactionSnapshotService;

    @Override
    public Account getAccountByIdAndUser(String accountId, String userId) {
        Optional<Account> optionalAccount = accountRepository.findAccountByIdForUser(accountId, userId);

        return optionalAccount.orElseThrow(() -> new AccountNotFoundException("not found"));
    }

    @Override
    public void updateBalanceForTransaction(BalanceUpdateRequest request) {
        // lock account for db transaction
        Account lockedAccount = accountRepository.lockAccount(request.getAccountId(), null)
                .orElseThrow(()-> new AccountNotFoundException("not found"));
        // 4. Calculate balance delta
        BigDecimal delta = calculateBalanceDelta(request.getTransactionType(), BigDecimal.valueOf(request.getAmount()), lockedAccount.getType());

        BigDecimal previousBalance = lockedAccount.getBalanceCached()==null?lockedAccount.getOpeningBalance():lockedAccount.getBalanceCached();
        BigDecimal newBalance = (lockedAccount.getBalanceCached() != null ?
                lockedAccount.getBalanceCached() : lockedAccount.getOpeningBalance()).add(delta);

        lockedAccount.setBalanceCached(newBalance);
        lockedAccount.setBalanceAsOf(LocalDateTime.now());

        accountRepository.save(lockedAccount);
        accountTransactionSnapshotService.createSnapshot(new SnapshotCreateRequest(
                lockedAccount.getId(),
                request.getTransactionId(),
                previousBalance,
                newBalance,
                request.getAmount()
        ));

    }

    @Override
    public Account createAccount(AccountCreateUpdateRequest request) {
        Account newAccount = Account.builder()
                .createdAt(LocalDateTime.now())
                .openingBalance(BigDecimal.valueOf(request.openingBalance()))
                .paymentDueDay(request.paymentDueDay())
                .label(request.label())
                .active(true)
                .balanceAsOf(LocalDateTime.now())
                .cardNetwork(request.cardNetwork())
                .closedAt(null)
                .balanceCached(null)
                .creditLimit(request.creditLimit())
                .currency(request.currency())
                .lastFour(request.lastFour())
                .readOnly(false)
                .type(AccountType.valueOf(request.accountType()))
                .build();

        return accountRepository.save(newAccount);
    }

    @Override
    public List<Account> getAccounts() {
        return accountRepository.findAll();
    }

    private BigDecimal calculateBalanceDelta(TransactionType type, BigDecimal amount, AccountType accountType) {
        if (accountType == AccountType.CARD) {
            // Credit card: purchase increases debt, payment reduces debt
            return type == TransactionType.EXPENSE ? amount : amount.negate();
        } else {
            // Bank/Cash/Wallet: expense reduces balance, income increases balance
            return type == TransactionType.EXPENSE ? amount.negate() : amount;
        }
    }

}
