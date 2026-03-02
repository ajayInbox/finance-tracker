package com.finance.tracker.accounts.controller;

import com.finance.tracker.accounts.domain.AccountCategory;
import com.finance.tracker.accounts.domain.AccountCreateUpdateRequest;
import com.finance.tracker.accounts.domain.AccountType;
import com.finance.tracker.accounts.domain.NetworthSummary;
import com.finance.tracker.accounts.domain.dto.AccountResponse;
import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.accounts.mapper.AccountMapper;
import com.finance.tracker.accounts.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountController accountController;

    private UUID predefinedUserId = UUID.fromString("960bbe86-b62c-4171-a8e5-94c4bfd3bdb4");
    private UUID accountId = UUID.randomUUID();
    private Account account;
    private AccountResponse accountResponse;
    private AccountCreateUpdateRequest createRequest;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(accountId);
        account.setAccountName("My Bank");

        accountResponse = new AccountResponse(
                accountId.toString(),
                "My Bank",
                AccountType.BANK,
                "1234",
                "INR",
                LocalDate.now(),
                new BigDecimal("100.00"),
                null,
                "0",
                "0",
                null,
                null,
                true,
                false,
                LocalDateTime.now(),
                null,
                "Notes",
                AccountCategory.ASSET
        );

        createRequest = new AccountCreateUpdateRequest(
                "My Bank", AccountType.BANK, "1234", "INR", new BigDecimal("100.00"), null, 0, 0, null, "Notes", AccountCategory.ASSET
        );
    }

    @Test
    void testAddAccount() {
        when(accountService.create(eq(predefinedUserId), any(AccountCreateUpdateRequest.class))).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        ResponseEntity<AccountResponse> response = accountController.add(createRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(accountResponse, response.getBody());
    }

    @Test
    void testGetAccounts() {
        when(accountService.getAccounts(predefinedUserId)).thenReturn(List.of(account));
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        ResponseEntity<List<AccountResponse>> response = accountController.getAccounts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(accountResponse, response.getBody().get(0));
    }

    @Test
    void testGetNetWorth() {
        NetworthSummary summary = NetworthSummary.builder()
                .assets(new NetworthSummary.ValueNumber(new BigDecimal("100"), 1))
                .liabilities(new NetworthSummary.ValueNumber(new BigDecimal("50"), 1))
                .netWorth(new BigDecimal("50"))
                .build();

        when(accountService.getNetWorth(predefinedUserId)).thenReturn(summary);

        ResponseEntity<NetworthSummary> response = accountController.getNetWorth();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(summary, response.getBody());
    }

    @Test
    void testDelete() {
        ResponseEntity<?> response = accountController.delete(accountId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(accountService).deleteAccount(predefinedUserId, accountId);
    }

    @Test
    void testUpdate() {
        when(accountService.update(eq(predefinedUserId), eq(accountId), any(AccountCreateUpdateRequest.class))).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        ResponseEntity<AccountResponse> response = accountController.update(accountId, createRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(accountResponse, response.getBody());
    }
}
