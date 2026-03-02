package com.finance.tracker.accounts.service.impl;

import com.finance.tracker.accounts.domain.SnapshotCreateRequest;
import com.finance.tracker.accounts.domain.entities.AccountTransactionSnapshot;
import com.finance.tracker.accounts.repository.AccountTransactionSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountTransactionSnapshotServiceImplTest {

    @Mock
    private AccountTransactionSnapshotRepository repository;

    @InjectMocks
    private AccountTransactionSnapshotServiceImpl service;

    @Test
    void testCreateSnapshot() {
        UUID accountId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        BigDecimal prevBalance = new BigDecimal("100.00");
        BigDecimal newBalance = new BigDecimal("150.00");
        BigDecimal amount = new BigDecimal("50.00");

        SnapshotCreateRequest request = new SnapshotCreateRequest(
                accountId, transactionId, prevBalance, newBalance, amount
        );

        service.createSnapshot(request);

        ArgumentCaptor<AccountTransactionSnapshot> captor = ArgumentCaptor.forClass(AccountTransactionSnapshot.class);
        verify(repository).save(captor.capture());

        AccountTransactionSnapshot savedSnapshot = captor.getValue();
        assertNotNull(savedSnapshot);
        assertEquals(accountId, savedSnapshot.getAccountId());
        assertEquals(transactionId, savedSnapshot.getTransactionId());
        assertEquals(prevBalance, savedSnapshot.getBalanceBefore());
        assertEquals(newBalance, savedSnapshot.getBalanceAfter());
        assertEquals(amount, savedSnapshot.getTransactionAmount());
        assertNotNull(savedSnapshot.getCreatedAt());
    }
}
