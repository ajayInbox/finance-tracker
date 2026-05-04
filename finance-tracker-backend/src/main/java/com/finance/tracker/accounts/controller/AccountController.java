package com.finance.tracker.accounts.controller;

import com.finance.tracker.accounts.domain.AccountCreateUpdateRequest;
import com.finance.tracker.accounts.domain.NetworthSummary;
import com.finance.tracker.accounts.domain.dto.AccountResponse;
import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.accounts.mapper.AccountMapper;
import com.finance.tracker.accounts.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@CrossOrigin
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @PostMapping("/account")
    public ResponseEntity<AccountResponse> add(@RequestBody AccountCreateUpdateRequest request, Authentication auth) {
        String userId = (String) auth.getPrincipal();
        Account account = accountService.create(UUID.fromString(userId), request);
        return new ResponseEntity<>(accountMapper.toResponse(account), HttpStatus.CREATED);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountResponse>> getAccounts(Authentication auth){
        String userId = (String) auth.getPrincipal();
        List<Account> accounts = accountService.getAccounts(UUID.fromString(userId));
        List<AccountResponse> res = accounts.stream().map(accountMapper::toResponse).toList();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/networth")
    public ResponseEntity<NetworthSummary> getNetWorth(Authentication auth){
        String userId = (String) auth.getPrincipal();
        NetworthSummary netWorth = accountService.getNetWorth(UUID.fromString(userId));
        return ResponseEntity.ok(netWorth);
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") UUID accountId,  Authentication auth) {
        String userId = (String) auth.getPrincipal();
        accountService.deleteAccount(UUID.fromString(userId), accountId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/accounts/{id}")
    public ResponseEntity<AccountResponse> update(@PathVariable("id") UUID accountId, @RequestBody AccountCreateUpdateRequest request,  Authentication auth) {
        String userId = (String) auth.getPrincipal();
        Account account = accountService.update(UUID.fromString(userId), accountId, request);
        return ResponseEntity.ok(accountMapper.toResponse(account));
    }
}
