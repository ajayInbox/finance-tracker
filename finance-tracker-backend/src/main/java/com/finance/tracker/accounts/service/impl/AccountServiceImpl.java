package com.finance.tracker.accounts.service.impl;

import com.finance.tracker.accounts.domain.*;
import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.accounts.exceptions.*;
import com.finance.tracker.accounts.mapper.AccountMapper;
import com.finance.tracker.accounts.repository.AccountRepository;
import com.finance.tracker.accounts.service.AccountService;
import com.finance.tracker.transactions.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Account getAccountByIdAndUser(String accountId, String userId) {
        return accountRepository.findAccountByIdForUser(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    protected List<Account> getAccountsByUserId(String userId) {
        return accountRepository.findByUserIdAndIsActive(userId);
    }

    @Override
    @Transactional
    public void updateBalanceForTransaction(BalanceUpdateRequest request) {

        String userId = null;
        // 1. Lock the account for transaction
        Account lockedAccount = accountRepository.lockAccount(request.getAccountId(), userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        BigDecimal amount = request.getAmount();
        BigDecimal previousBalance = getEffectiveBalance(lockedAccount);
        BigDecimal newBalance = BigDecimal.ZERO;

        // 2. APPLY CORRECT EFFECT BASED ON CATEGORY
        if (lockedAccount.getCategory() == AccountCategory.ASSET) {

            // Bank/Cash/Wallet
            BigDecimal delta = (request.getTransactionType() == TransactionType.EXPENSE)
                    ? amount.negate()
                    : amount;

            newBalance = previousBalance.add(delta);
            if(newBalance.doubleValue()<0){
                throw new AccountAmountNegativeException("Account balance should not be negative");
            }
            lockedAccount.setCurrentBalance(newBalance);

        } else if (lockedAccount.getCategory() == AccountCategory.LIABILITY) {

            // Credit Card / Loan
            // EXPENSE → increase outstanding
            // INCOME → reduce outstanding
            BigDecimal creditLimit = lockedAccount.getCreditLimit();
            BigDecimal delta = (request.getTransactionType() == TransactionType.EXPENSE)
                    ? amount
                    : amount.negate();
            BigDecimal currentCreditLimit = creditLimit.add(newBalance.negate());
            
            newBalance = previousBalance.add(delta);
            if(delta.doubleValue()>currentCreditLimit.doubleValue()){
                throw new AmountGtCurrentBalance("Amount is greater than current credit limit");
            }
            if(newBalance.doubleValue()<0){
                throw new AccountAmountNegativeException("Current outstanding should not be negative");
            }
            lockedAccount.setCurrentOutstanding(newBalance);
        }

        lockedAccount.setBalanceAsOf(Instant.now());
        accountRepository.save(lockedAccount);

        // 3. Create Snapshot
        eventPublisher.publishEvent(
                new ATSnapshotCreateEvent(
                        this,
                        lockedAccount.getId(),
                        request.getTransactionId(),
                        previousBalance,
                        newBalance,
                        request.getAmount()
                )
        );
    }

    @Override
    public Account create(String userId, AccountCreateUpdateRequest req) {
        ensureLastFourNotDuplicate(req.lastFour(), userId, req.accountType());

        Account account = mapper.toEntity(req, userId);
        return accountRepository.save(account);
    }

    @Override
    public Account update(String userId, String id, AccountCreateUpdateRequest req) {
        Account entity = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // If lastFour changed → enforce duplicate check for same type
        if (!entity.getLastFour().equals(req.lastFour()) ||
                entity.getAccountType() != req.accountType()) {

            ensureLastFourNotDuplicate(req.lastFour(), userId, req.accountType());
        }

        mapper.updateEntity(entity, req);
        return accountRepository.save(entity);
    }

    @Override
    public List<Account> getAccounts() {
        return getAccountsByUserId(null);
    }

    @Override
    public String getAccountByLastFour(String lastFour){
        Account account = accountRepository.findByLastFour(lastFour)
                .orElseThrow(() -> new AccountNotFoundException("not found"));
        return account.getId();
    }

    // -------------------------------------------------------------------------
    // Net Worth
    // -------------------------------------------------------------------------

    @Override
    public NetworthSummary getNetWorth(String userId) {
        List<Account> accounts = getAccountsByUserId(userId);

        BigDecimal assetValue = BigDecimal.ZERO;
        BigDecimal liabilityValue = BigDecimal.ZERO;
        int assetAccounts = 0;
        int liabilityAccounts = 0;

        for (Account account : accounts) {
            BigDecimal balance = getEffectiveBalance(account);

            if (account.isAsset()) {
                assetAccounts += 1;
                assetValue = assetValue.add(balance);
            } else if (account.isLiability()) {
                liabilityAccounts += 1;
                liabilityValue = liabilityValue.add(balance);
            }
        }

        BigDecimal totalNetWorth = assetValue.subtract(liabilityValue);

        return NetworthSummary.builder()
                .assets(new NetworthSummary.ValueNumber(
                        assetValue,
                        assetAccounts
                ))
                .liabilities(new NetworthSummary.ValueNumber(
                        liabilityValue,
                        liabilityAccounts
                ))
                .netWorth(totalNetWorth)
                .build();
    }

    @Override
    public void deleteAccount(String accountId) {
        Account account = getAccountByIdAndUser(accountId, null);
        account.setActive(false);
        account.setClosedAt(Instant.now());
        account.setStatus(AccountStatus.INACTIVE);
        accountRepository.save(account);
    }

    private void ensureLastFourNotDuplicate(String lastFour, String userId, AccountType type) {
        accountRepository
                .findByLastFourAndUserIdAndAccountType(lastFour, userId, type)
                .ifPresent(a -> {
                    throw new DuplicateLastFourException(
                            "Another " + type + " account already has last four digits " + lastFour
                    );
                });
    }

    private BigDecimal getEffectiveBalance(Account account) {

        // 1 — Credit Card Logic (Liability)
        if (account.isLiability()) {
            return Optional.ofNullable(account.getCurrentOutstanding())
                    .orElse(BigDecimal.ZERO);
        }

        // 2 — Bank / Asset Logic
        return Optional.ofNullable(account.getCurrentBalance())
                .or(() -> Optional.ofNullable(account.getStartingBalance()))
                .orElse(BigDecimal.ZERO);
    }
}
