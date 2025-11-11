package com.finance.tracker.accounts.controller;

import com.finance.tracker.accounts.domain.AccountCreateUpdateRequest;
import com.finance.tracker.accounts.domain.NetworthSummary;
import com.finance.tracker.accounts.domain.dto.AccountDto;
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
    public ResponseEntity<AccountDto> addAccount(@RequestBody AccountCreateUpdateRequest request){
        Account account = accountService.createAccount(request);
        return new ResponseEntity<>(accountMapper.toDto(account), HttpStatus.CREATED);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDto>> getAccounts(){
        List<Account> accounts = accountService.getAccounts();
        List<AccountDto> res = accounts.stream().map(accountMapper::toDto).toList();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/networth")
    public ResponseEntity<NetworthSummary> getNetWorth(@RequestParam(required = false, name = "userId") String userId){
        NetworthSummary netWorth = accountService.getNetWorth(null);
        return ResponseEntity.ok(netWorth);
    }
}
