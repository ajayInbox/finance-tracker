package com.finance.tracker.accounts.service.impl;

import com.finance.tracker.accounts.domain.ATSnapshotCreateEvent;
import com.finance.tracker.accounts.domain.AccountCategory;
import com.finance.tracker.accounts.domain.AccountCreateUpdateRequest;
import com.finance.tracker.accounts.domain.AccountStatus;
import com.finance.tracker.accounts.domain.AccountType;
import com.finance.tracker.accounts.domain.BalanceUpdateRequest;
import com.finance.tracker.accounts.domain.NetworthSummary;
import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.accounts.exceptions.AccountNotFoundException;
import com.finance.tracker.accounts.exceptions.AccountUpdateFailedException;
import com.finance.tracker.accounts.exceptions.DuplicateLastFourException;
import com.finance.tracker.accounts.mapper.AccountMapper;
import com.finance.tracker.accounts.repository.AccountRepository;
import com.finance.tracker.transactions.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AccountServiceImpl accountService;

    private UUID userId;
    private UUID accountId;
    private Account assetAccount;
    private Account liabilityAccount;
    private AccountCreateUpdateRequest createRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        assetAccount = new Account();
        assetAccount.setId(accountId);
        assetAccount.setUserId(userId);
        assetAccount.setAccountType(AccountType.BANK);
        assetAccount.setCategory(AccountCategory.ASSET);
        assetAccount.setLastFour("1234");
        assetAccount.setCurrentBalance(new BigDecimal("1000.00"));
        assetAccount.setActive(true);

        liabilityAccount = new Account();
        liabilityAccount.setId(UUID.randomUUID());
        liabilityAccount.setUserId(userId);
        liabilityAccount.setAccountType(AccountType.CREDIT_CARD);
        liabilityAccount.setCategory(AccountCategory.LIABILITY);
        liabilityAccount.setLastFour("5678");
        liabilityAccount.setCurrentOutstanding(new BigDecimal("500.00"));
        liabilityAccount.setActive(true);

        createRequest = new AccountCreateUpdateRequest(
                "My Savings", AccountType.BANK, "1234", "INR", new BigDecimal("1000.00"), null, 0, 0, null, "Notes", AccountCategory.ASSET
        );
    }

    @Test
    void testGetAccountByIdAndUser_Success() {
        when(accountRepository.findAccountByIdForUser(accountId, userId)).thenReturn(Optional.of(assetAccount));

        Account result = accountService.getAccountByIdAndUser(accountId, userId);
        assertNotNull(result);
        assertEquals(accountId, result.getId());
        verify(accountRepository).findAccountByIdForUser(accountId, userId);
    }

    @Test
    void testGetAccountByIdAndUser_NotFound() {
        when(accountRepository.findAccountByIdForUser(accountId, userId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountByIdAndUser(accountId, userId));
    }

    @Test
    void testUpdateBalanceForTransaction_AssetExpense_Success() {
        UUID transactionId = UUID.randomUUID();
        BalanceUpdateRequest req = new BalanceUpdateRequest(accountId, new BigDecimal("100.00"), TransactionType.EXPENSE, transactionId);

        when(accountRepository.findAccountByIdForUser(accountId, userId)).thenReturn(Optional.of(assetAccount));
        when(accountRepository.updateAssetBalance(eq(accountId), eq(userId), eq(new BigDecimal("-100.00")))).thenReturn(1);

        accountService.updateBalanceForTransaction(req, userId);

        verify(accountRepository).updateAssetBalance(accountId, userId, new BigDecimal("-100.00"));
        verifyEventPublished();
    }

    @Test
    void testUpdateBalanceForTransaction_LiabilityExpense_Success() {
        UUID transactionId = UUID.randomUUID();
        UUID liabilityAccountId = liabilityAccount.getId();
        BalanceUpdateRequest req = new BalanceUpdateRequest(liabilityAccountId, new BigDecimal("100.00"), TransactionType.EXPENSE, transactionId);

        when(accountRepository.findAccountByIdForUser(liabilityAccountId, userId)).thenReturn(Optional.of(liabilityAccount));
        when(accountRepository.updateLiabilityBalance(eq(liabilityAccountId), eq(userId), eq(new BigDecimal("100.00")))).thenReturn(1);

        accountService.updateBalanceForTransaction(req, userId);

        verify(accountRepository).updateLiabilityBalance(liabilityAccountId, userId, new BigDecimal("100.00"));
        verifyEventPublished();
    }

    @Test
    void testUpdateBalanceForTransaction_FailureThrowsException() {
        UUID transactionId = UUID.randomUUID();
        BalanceUpdateRequest req = new BalanceUpdateRequest(accountId, new BigDecimal("2000.00"), TransactionType.EXPENSE, transactionId);

        when(accountRepository.findAccountByIdForUser(accountId, userId)).thenReturn(Optional.of(assetAccount));
        when(accountRepository.updateAssetBalance(any(), any(), any())).thenReturn(0);

        assertThrows(AccountUpdateFailedException.class, () -> accountService.updateBalanceForTransaction(req, userId));
    }

    @Test
    void testCreate_Success() {
        when(accountRepository.findByLastFourAndUserIdAndAccountType("1234", userId, AccountType.BANK))
                .thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(assetAccount);

        Account result = accountService.create(userId, createRequest);
        assertNotNull(result);
        assertEquals("1234", result.getLastFour());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void testCreate_DuplicateLastFour() {
        when(accountRepository.findByLastFourAndUserIdAndAccountType("1234", userId, AccountType.BANK))
                .thenReturn(Optional.of(assetAccount));

        assertThrows(DuplicateLastFourException.class, () -> accountService.create(userId, createRequest));
    }

    @Test
    void testUpdate_Success() {
        when(accountRepository.findAccountByIdForUser(accountId, userId)).thenReturn(Optional.of(assetAccount));
        when(accountRepository.save(assetAccount)).thenReturn(assetAccount);

        Account result = accountService.update(userId, accountId, createRequest);
        assertNotNull(result);
        verify(accountMapper).updateEntity(assetAccount, createRequest);
        verify(accountRepository).save(assetAccount);
    }

    @Test
    void testGetAccounts() {
        when(accountRepository.findByUserIdAndActiveTrue(userId)).thenReturn(List.of(assetAccount, liabilityAccount));

        List<Account> accounts = accountService.getAccounts(userId);
        assertEquals(2, accounts.size());
    }

    @Test
    void testGetNetWorth() {
        when(accountRepository.findByUserIdAndActiveTrue(userId)).thenReturn(List.of(assetAccount, liabilityAccount));

        NetworthSummary summary = accountService.getNetWorth(userId);
        assertNotNull(summary);
        assertEquals(new BigDecimal("1000.00"), summary.getAssets().getTotal());
        assertEquals(1, summary.getAssets().getNumber());
        assertEquals(new BigDecimal("500.00"), summary.getLiabilities().getTotal());
        assertEquals(1, summary.getLiabilities().getNumber());
        assertEquals(new BigDecimal("500.00"), summary.getNetWorth());
    }

    @Test
    void testDeleteAccount() {
        when(accountRepository.findAccountByIdForUser(accountId, userId)).thenReturn(Optional.of(assetAccount));

        accountService.deleteAccount(accountId, userId);

        assertFalse(assetAccount.isActive());
        assertEquals(AccountStatus.INACTIVE, assetAccount.getStatus());
        assertNotNull(assetAccount.getClosedAt());
        verify(accountRepository).save(assetAccount);
    }

    private void verifyEventPublished() {
        ArgumentCaptor<ATSnapshotCreateEvent> captor = ArgumentCaptor.forClass(ATSnapshotCreateEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertNotNull(captor.getValue());
    }
}
