package com.finance.tracker.accounts.service.impl;

import com.finance.tracker.accounts.domain.*;
import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.accounts.exceptions.*;
import com.finance.tracker.accounts.mapper.AccountMapper;
import com.finance.tracker.accounts.repository.AccountRepository;
import com.finance.tracker.accounts.service.AccountService;
import com.finance.tracker.transactions.domain.Currency;
import com.finance.tracker.transactions.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Account getAccountByIdAndUser(UUID accountId, UUID userId) {
        return accountRepository.findAccountByIdForUser(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found or access denied"));
    }

    @Override
    @Transactional
    public void updateBalanceForTransaction(BalanceUpdateRequest request, UUID userId) {
        // 1. Pre-fetch to identify Category and initial state
        Account account = getAccountByIdAndUser(request.getAccountId(), userId);

        BigDecimal delta;
        int rowsUpdated;

        if (account.isAsset()) {
            delta = (request.getTransactionType() == TransactionType.EXPENSE)
                    ? request.getAmount().negate() : request.getAmount();
            rowsUpdated = accountRepository.updateAssetBalance(account.getId(), userId, delta);
        } else {
            delta = (request.getTransactionType() == TransactionType.EXPENSE)
                    ? request.getAmount() : request.getAmount().negate();
            rowsUpdated = accountRepository.updateLiabilityBalance(account.getId(), userId, delta);
        }

        if (rowsUpdated == 0) {
            throw new AccountUpdateFailedException("Update failed: Insufficient funds or credit limit reached.");
        }

        // 2. Audit Snapshot
        BigDecimal oldBalance = getEffectiveBalance(account);
        eventPublisher.publishEvent(new ATSnapshotCreateEvent(
                this, account.getId(), request.getTransactionId(),
                oldBalance, oldBalance.add(delta), request.getAmount()
        ));
    }

    @Override
    @Transactional
    public Account create(UUID userId, AccountCreateUpdateRequest req) {
        ensureLastFourNotDuplicate(req.lastFour(), userId, req.accountType());

        Account account = Account.builder()
                .accountName(req.accountName())
                .currency(Currency.valueOf(req.currency()))
                .lastFour(req.lastFour())
                .accountType(req.accountType())
                .notes(req.notes())
                .category(req.category())
                .active(true)
                .readOnly(false)
                .userId(userId)
                .status(AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .openingDate(LocalDate.now())
                .build();
        if(req.category() == AccountCategory.ASSET) {
            account.setStartingBalance(req.startingBalance());
            account.setCurrentBalance(req.startingBalance());
        }else {
            account.setCreditLimit(req.creditLimit());
            account.setCurrentOutstanding(req.currentOutstanding());
            account.setDueDayOfMonth(req.dueDayOfMonth());
            account.setStatementDayOfMonth(req.statementDayOfMonth());
        }
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public Account update(UUID userId, UUID id, AccountCreateUpdateRequest req) {
        Account entity = getAccountByIdAndUser(id, userId);

        // Logic check: if lastFour or type changed, re-validate duplicates
        if (!entity.getLastFour().equals(req.lastFour()) || entity.getAccountType() != req.accountType()) {
            ensureLastFourNotDuplicate(req.lastFour(), userId, req.accountType());
        }

        mapper.updateEntity(entity, req);
        return accountRepository.save(entity);
    }

    @Override
    public List<Account> getAccounts(UUID userId) {
        return accountRepository.findByUserIdAndActiveTrue(userId);
    }

    @Override
    public NetworthSummary getNetWorth(UUID userId) {
        List<Account> accounts = getAccounts(userId);

        BigDecimal assetTotal = BigDecimal.ZERO;
        BigDecimal liabilityTotal = BigDecimal.ZERO;

        for (Account acc : accounts) {
            BigDecimal bal = getEffectiveBalance(acc);
            if (acc.isAsset()) assetTotal = assetTotal.add(bal);
            else liabilityTotal = liabilityTotal.add(bal);
        }

        return NetworthSummary.builder()
                .assets(new NetworthSummary.ValueNumber(assetTotal, (int) accounts.stream().filter(Account::isAsset).count()))
                .liabilities(new NetworthSummary.ValueNumber(liabilityTotal, (int) accounts.stream().filter(Account::isLiability).count()))
                .netWorth(assetTotal.subtract(liabilityTotal))
                .build();
    }

    @Override
    @Transactional
    public void deleteAccount(UUID accountId, UUID userId) {
        Account account = getAccountByIdAndUser(accountId, userId);
        account.setActive(false);
        account.setClosedAt(Instant.now());
        account.setStatus(AccountStatus.INACTIVE);
        accountRepository.save(account);
    }

    // --- Helpers ---

    private void ensureLastFourNotDuplicate(String lastFour, UUID userId, AccountType type) {
        accountRepository.findByLastFourAndUserIdAndAccountType(lastFour, userId, type)
                .ifPresent(a -> {
                    throw new DuplicateLastFourException("Another " + type + " account with last four " + lastFour + " exists.");
                });
    }

    private BigDecimal getEffectiveBalance(Account account) {
        if (account.isLiability()) {
            return account.getCurrentOutstanding() != null ? account.getCurrentOutstanding() : BigDecimal.ZERO;
        }
        return account.getCurrentBalance() != null ? account.getCurrentBalance() :
                (account.getStartingBalance() != null ? account.getStartingBalance() : BigDecimal.ZERO);
    }
}