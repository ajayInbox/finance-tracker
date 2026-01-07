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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@CrossOrigin
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @PostMapping("/account")
    public ResponseEntity<AccountResponse> addAccount(@RequestBody AccountCreateUpdateRequest request){
        Account account = accountService.create(null, request);
        return new ResponseEntity<>(accountMapper.toResponse(account), HttpStatus.CREATED);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountResponse>> getAccounts(){
        List<Account> accounts = accountService.getAccounts();
        List<AccountResponse> res = accounts.stream().map(accountMapper::toResponse).toList();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/networth")
    public ResponseEntity<NetworthSummary> getNetWorth(@RequestParam(required = false, name = "userId") String userId){
        NetworthSummary netWorth = accountService.getNetWorth(null);
        return ResponseEntity.ok(netWorth);
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") String accountId){
        accountService.deleteAccount(accountId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/accounts/{id}")
    public ResponseEntity<AccountResponse> update(@PathVariable("id") String accountId, @RequestBody AccountCreateUpdateRequest request){
        Account account = accountService.update(null, accountId, request);
        return ResponseEntity.ok(accountMapper.toResponse(account));
    }
}
