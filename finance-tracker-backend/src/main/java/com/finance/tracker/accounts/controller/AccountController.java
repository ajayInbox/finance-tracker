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
    public ResponseEntity<AccountResponse> add(@RequestBody AccountCreateUpdateRequest request){
        Account account = accountService.create(UUID.fromString("960bbe86-b62c-4171-a8e5-94c4bfd3bdb4"), request);
        return new ResponseEntity<>(accountMapper.toResponse(account), HttpStatus.CREATED);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountResponse>> getAccounts(){
        List<Account> accounts = accountService.getAccounts(UUID.fromString("960bbe86-b62c-4171-a8e5-94c4bfd3bdb4"));
        List<AccountResponse> res = accounts.stream().map(accountMapper::toResponse).toList();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/networth")
    public ResponseEntity<NetworthSummary> getNetWorth(){
        NetworthSummary netWorth = accountService.getNetWorth(UUID.fromString("960bbe86-b62c-4171-a8e5-94c4bfd3bdb4"));
        return ResponseEntity.ok(netWorth);
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") UUID accountId){
        accountService.deleteAccount(UUID.fromString("960bbe86-b62c-4171-a8e5-94c4bfd3bdb4"), accountId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/accounts/{id}")
    public ResponseEntity<AccountResponse> update(@PathVariable("id") UUID accountId, @RequestBody AccountCreateUpdateRequest request){
        Account account = accountService.update(UUID.fromString("960bbe86-b62c-4171-a8e5-94c4bfd3bdb4"), accountId, request);
        return ResponseEntity.ok(accountMapper.toResponse(account));
    }
}
