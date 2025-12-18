package com.finance.tracker.accounts.mapper;

import com.finance.tracker.accounts.domain.AccountCategory;
import com.finance.tracker.accounts.domain.AccountCreateUpdateRequest;
import com.finance.tracker.accounts.domain.dto.AccountResponse;
import com.finance.tracker.accounts.domain.entities.Account;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    // ---------- CREATE MAPPING ----------

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "readOnly", constant = "false")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "closedAt", ignore = true)
    @Mapping(target = "balanceAsOf", ignore = true)
    @Mapping(target = "currentBalance", ignore = true)
    @Mapping(target = "userId", source = "userId")
    Account toEntity(AccountCreateUpdateRequest req, String userId);

    // ---------- UPDATE EXISTING ENTITY ----------

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "closedAt", ignore = true)
    @Mapping(target = "currentBalance", ignore = true)
    void updateEntity(@MappingTarget Account entity, AccountCreateUpdateRequest req);

    // ---------- ENTITY → RESPONSE ----------

    @Mapping(target = "createdAt", expression = "java(com.finance.tracker.accounts.utils.DateTimeMapper.toLocalDateTime(account.getCreatedAt()))")
    @Mapping(target = "closedAt", expression = "java(com.finance.tracker.accounts.utils.DateTimeMapper.toLocalDateTime(account.getClosedAt()))")
    AccountResponse toResponse(Account account);

    // ---------- CUSTOM LOGIC FOR ASSET / LIABILITY -----------

    @AfterMapping
    default void applyCategorySpecificFields(
            @MappingTarget Account entity,
            AccountCreateUpdateRequest req
    ) {

        if (req.category() == AccountCategory.ASSET) {

            entity.setCurrentOutstanding(null);
            entity.setCreditLimit(null);
            entity.setCutoffDayOfMonth(null);
            entity.setDueDayOfMonth(null);

            entity.setCurrentBalance(req.startingBalance());
            entity.setStartingBalance(req.startingBalance());
        }

        if (req.category() == AccountCategory.LIABILITY) {

            entity.setStartingBalance(null);
            entity.setCurrentBalance(null);

            entity.setCurrentOutstanding(req.currentOutstanding());
            entity.setCreditLimit(req.creditLimit());
            entity.setCutoffDayOfMonth(req.cutoffDayOfMonth());
            entity.setDueDayOfMonth(req.dueDayOfMonth());
        }
    }
}

