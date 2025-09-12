package com.finance.tracker.accounts.mapper;

import com.finance.tracker.accounts.domain.dto.AccountDto;
import com.finance.tracker.accounts.domain.entities.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountDto toDto(Account account);
}
