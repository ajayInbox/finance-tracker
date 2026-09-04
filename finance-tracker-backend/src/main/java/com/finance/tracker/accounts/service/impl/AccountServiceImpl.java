package com.finance.tracker.accounts.service.impl;

import com.finance.tracker.accounts.domain.*;
import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.accounts.exceptions.*;
import com.finance.tracker.accounts.mapper.AccountMapper;
import com.finance.tracker.accounts.repository.AccountRepository;
import com.finance.tracker.accounts.service.AccountService;
import com.finance.tracker.auth.domain.DashboardMode;
import com.finance.tracker.auth.domain.entity.User;
import com.finance.tracker.auth.repository.UserRepository;
import com.finance.tracker.transactions.domain.Currency;
import com.finance.tracker.transactions.domain.TransactionType;
import com.finance.tracker.category.domain.entities.Category;
import com.finance.tracker.category.domain.CategoryType;
import com.finance.tracker.category.repository.CategoryRepository;
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
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AccountMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Account getAccountByIdAndUser(UUID accountId, UUID userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found or access denied"));
    }

    @Override
    public void updateBalance(UUID accountId, UUID userId, UUID transactionId, TransactionType type, BigDecimal amount){
        // 1. Pre-fetch to identify Category and initial state
        Account account = getAccountByIdAndUser(accountId, userId);

        BigDecimal delta;
        int rowsUpdated;

        if (account.isAsset()) {
            delta = (type == TransactionType.EXPENSE)
                    ? amount.negate() : amount;
            rowsUpdated = accountRepository.updateAssetBalance(account.getId(), userId, delta);
        } else {
            delta = (type == TransactionType.EXPENSE)
                    ? amount : amount.negate();
            rowsUpdated = accountRepository.updateLiabilityBalance(account.getId(), userId, delta);
        }

        if (rowsUpdated == 0) {
            throw new AccountUpdateFailedException("Update failed: Insufficient funds or credit limit reached.");
        }

        // 2. Audit Snapshot
        BigDecimal oldBalance = getEffectiveBalance(account);
        eventPublisher.publishEvent(new ATSnapshotCreateEvent(
                this, account.getId(), transactionId,
                oldBalance, oldBalance.add(delta), amount
        ));
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
        String normalizedLastFour = normalizeLastFour(req.lastFour(), req.accountType());
        ensureLastFourNotDuplicate(normalizedLastFour, userId, req.accountType());

        Currency currency = Currency.INR;
        if (req.currency() != null && !req.currency().isBlank()) {
            try {
                currency = Currency.valueOf(req.currency().toUpperCase().trim());
            } catch (Exception ignored) {}
        }

        String accountName = req.accountName() != null && !req.accountName().isBlank()
                ? req.accountName()
                : req.name();

        Account account = Account.builder()
                .accountName(accountName)
                .currency(currency)
                .lastFour(normalizedLastFour)
                .institution(req.institution())
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

        BigDecimal balance = req.balance();
        if (req.category() == AccountCategory.ASSET) {
            BigDecimal assetBal = req.startingBalance() != null ? req.startingBalance() : balance;
            account.setStartingBalance(assetBal);
            account.setCurrentBalance(assetBal);
        } else {
            BigDecimal liabBal = req.currentOutstanding() != null ? req.currentOutstanding() : balance;
            account.setCreditLimit(req.creditLimit());
            account.setCurrentOutstanding(liabBal);
            account.setDueDayOfMonth(req.dueDayOfMonth());
            account.setStatementDayOfMonth(req.statementDayOfMonth());
        }
        Account saved = accountRepository.save(account);
        updateDashboardModeIfNeeded(userId);
        return saved;
    }

    @Override
    @Transactional
    public Account update(UUID userId, UUID id, AccountCreateUpdateRequest req) {
        Account entity = getAccountByIdAndUser(id, userId);

        String normalizedLastFour = req.lastFour() != null && !req.lastFour().isBlank()
                ? normalizeLastFour(req.lastFour(), req.accountType())
                : entity.getLastFour();

        // Logic check: if lastFour or type changed, re-validate duplicates
        if (!entity.getLastFour().equals(normalizedLastFour) || entity.getAccountType() != req.accountType()) {
            ensureLastFourNotDuplicate(normalizedLastFour, userId, req.accountType());
        }

        mapper.updateEntity(entity, req);
        entity.setLastFour(normalizedLastFour);
        if (req.institution() != null) {
            entity.setInstitution(req.institution());
        }
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
    public Account initializeDefaults(UUID userId) {
        Account defaultAccount = accountRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseGet(() -> {
                    Account newAcc = Account.builder()
                            .accountName("General Cash")
                            .accountType(AccountType.CASH)
                            .category(AccountCategory.ASSET)
                            .lastFour("CASH")
                            .currency(Currency.INR)
                            .startingBalance(BigDecimal.ZERO)
                            .currentBalance(BigDecimal.ZERO)
                            .openingDate(LocalDate.now())
                            .userId(userId)
                            .status(AccountStatus.ACTIVE)
                            .active(true)
                            .isDefault(true)
                            .build();
                    return accountRepository.save(newAcc);
                });

        if (categoryRepository.findByUserIdAndName(userId, "Uncategorized").isEmpty()) {
            Category parentGroup = Category.builder()
                    .name("General")
                    .description("Default category group")
                    .type(CategoryType.EXPENSE)
                    .userId(userId)
                    .isActive(true)
                    .iconKey("58729+MaterialIcons")
                    .colorCode("4289470940")
                    .build();
            categoryRepository.save(parentGroup);

            Category defaultCategory = Category.builder()
                    .name("Uncategorized")
                    .description("Default expense category")
                    .type(CategoryType.EXPENSE)
                    .userId(userId)
                    .isActive(true)
                    .parent(parentGroup)
                    .iconKey("58729+MaterialIcons")
                    .colorCode("4289470940")
                    .build();
            categoryRepository.save(defaultCategory);
        }

        updateDashboardModeIfNeeded(userId);
        return defaultAccount;
    }

    @Override
    @Transactional
    public void deleteAccount(UUID accountId, UUID userId) {
        Account account = getAccountByIdAndUser(accountId, userId);
        account.setActive(false);
        account.setClosedAt(Instant.now());
        account.setStatus(AccountStatus.INACTIVE);
        accountRepository.save(account);
        updateDashboardModeIfNeeded(userId);
    }

    // --- Helpers ---

    private void updateDashboardModeIfNeeded(UUID userId) {
        List<Account> activeAccounts = accountRepository.findByUserIdAndActiveTrue(userId);
        DashboardMode targetMode = activeAccounts.isEmpty()
                ? DashboardMode.EXPENSE_ONLY
                : DashboardMode.EXPENSE_AND_ACCOUNT;

        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getDashboardMode() != targetMode) {
            user.setDashboardMode(targetMode);
            userRepository.save(user);
        }
    }

    private String normalizeLastFour(String lastFour, AccountType type) {
        if (lastFour == null || lastFour.isBlank()) {
            return (type == AccountType.CASH) ? "CASH" : "0000";
        }
        String digits = lastFour.replaceAll("\\D", "");
        if (digits.length() >= 4) {
            return digits.substring(digits.length() - 4);
        }
        if (!digits.isEmpty()) {
            return String.format("%4s", digits).replace(' ', '0');
        }
        String trimmed = lastFour.trim();
        if (trimmed.length() >= 4) {
            return trimmed.substring(trimmed.length() - 4);
        }
        return String.format("%4s", trimmed).replace(' ', '0');
    }

    private void ensureLastFourNotDuplicate(String lastFour, UUID userId, AccountType type) {
        if ("0000".equals(lastFour) || "CASH".equals(lastFour)) {
            return;
        }
        accountRepository.findByLastFourAndUserIdAndAccountType(lastFour, userId, type)
                .ifPresent(a -> {
                    throw new DuplicateLastFourException("Another %s account with last four %s exists.".formatted(type, lastFour));
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