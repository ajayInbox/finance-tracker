package com.finance.tracker.accounts.service.impl;

import com.finance.tracker.accounts.domain.*;
import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.accounts.exceptions.AccountNotFoundException;
import com.finance.tracker.accounts.repository.AccountRepository;
import com.finance.tracker.accounts.service.AccountService;
import com.finance.tracker.accounts.service.AccountTransactionSnapshotService;
import com.finance.tracker.transactions.domain.TransactionType;
import com.finance.tracker.transactions.domain.entities.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;
    private final static List<String> ASSET_TYPE = List.of("bank", "wallet", "cash");

    @Override
    public Account getAccountByIdAndUser(String accountId, String userId) {
        Optional<Account> optionalAccount = accountRepository.findAccountByIdForUser(accountId, userId);

        return optionalAccount.orElseThrow(() -> new AccountNotFoundException("not found"));
    }

    private List<Account> getAccountByUserId(String userId){
        return accountRepository.findByUserIdAndIsActive(userId);
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
                previousBalance.doubleValue(),
                newBalance.doubleValue(),
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
                .isAsset(ASSET_TYPE.contains(request.accountType().toLowerCase()))
                .isLiability(!ASSET_TYPE.contains(request.accountType().toLowerCase()))
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

    @Override
    public String getAccountByLastFour(String lastFour){
        Account account = accountRepository.findByLastFour(lastFour)
                .orElseThrow(() -> new AccountNotFoundException("not found"));
        return account.getId();
    }

    @Override
    public void updateAccountBalance(Transaction transaction) {
        Account account = accountRepository.findById(transaction.getAccount())
                .orElseThrow(() -> new AccountNotFoundException("not found"));

        var previousBalance = account.getBalanceCached();
        applyTransactionEffect(account, transaction);
        account.setBalanceAsOf(LocalDateTime.now());

        Account savedAccount = accountRepository.save(account);

        eventPublisher.publishEvent(
                new ATSnapshotCreateEvent(
                        this,
                        account.getId(),
                        transaction.getId(),
                        previousBalance.doubleValue(),
                        savedAccount.getBalanceCached().doubleValue(),
                        transaction.getAmount()
                )
        );
    }

    @Override
    public NetworthSummary getNetWorth(String userId) {
        List<Account> accounts = getAccountByUserId(userId);
        BigDecimal assetValue = BigDecimal.ZERO;
        BigDecimal liabilityValue = BigDecimal.ZERO;
        int assetAccounts = 0;
        int liabilityAccounts = 0;

        for (Account account : accounts){
            BigDecimal balance = account.getBalanceCached() != null
                    ? account.getBalanceCached()
                    : account.getOpeningBalance() != null ? account.getOpeningBalance() : BigDecimal.ZERO;
            if (account.isAsset()) {
                assetAccounts+=1;
                assetValue = assetValue.add(balance);
            } else {
                liabilityAccounts+=1;
                liabilityValue = liabilityValue.add(balance);
            }
        }

        BigDecimal totalNetWorth = assetValue.subtract(liabilityValue);
        return NetworthSummary.builder()
                .assets(new NetworthSummary.ValueNumber(assetValue.doubleValue(), assetAccounts))
                .liabilities(new NetworthSummary.ValueNumber(liabilityValue.doubleValue(), liabilityAccounts))
                .netWorth(totalNetWorth.doubleValue())
                .build();
    }


    private void applyTransactionEffect(Account account, Transaction txn) {
        double balance = account.getBalanceCached().doubleValue();
        double amount = txn.getAmount();

        if (txn.getType()==TransactionType.EXPENSE) {
            balance -= amount;
        } else if (txn.getType()==TransactionType.INCOME) {
            balance += amount;
        }

        account.setBalanceCached(BigDecimal.valueOf(balance));
    }


}
