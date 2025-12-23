package com.finance.tracker.transactions.mapper;

import com.finance.tracker.transactions.domain.*;
import com.finance.tracker.transactions.domain.dtos.*;
import com.finance.tracker.transactions.domain.entities.Transaction;

import java.math.BigDecimal;
import org.mapstruct.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {

    String DEFAULT_STATUS = "ACTIVE";
    String DEFAULT_ACTION = "CREATED";

    ZoneId APP_ZONE_ID = ZoneId.of("Asia/Kolkata");
    DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ---------- MAIN CREATION MAPPER ----------

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", expression = "java(mapType(request.getType()))")
    @Mapping(target = "occurredAt", expression = "java(toInstant(request.getOccurredAt()))")
    @Mapping(target = "postedAt", expression = "java(toInstant(request.getPostedAt()))")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "lastAction", constant = DEFAULT_ACTION)
    @Mapping(target = "status", constant = DEFAULT_STATUS)
    @Mapping(target = "reversalOf", ignore = true)
    Transaction toNewEntity(CreateTransactionRequest request, String userId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", expression = "java(mapType(request.getType()))")
    @Mapping(target = "occurredAt", expression = "java(toInstant(request.getOccurredAt()))")
    @Mapping(target = "postedAt", expression = "java(toInstant(request.getPostedAt()))")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "lastAction", constant = DEFAULT_ACTION)
    @Mapping(target = "status", constant = DEFAULT_STATUS)
    @Mapping(target = "reversalOf", ignore = true)
    Transaction toNewEntity(UpdateTransactionRequest request, String userId);

    // ---------- UPDATE EXISTING ENTITY (non-structural updates only) ----------

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "transactionName", source = "request.transactionName"),
            @Mapping(target = "attachments", source = "request.attachments"),
            @Mapping(target = "category", source = "request.category"),
            @Mapping(target = "currency", source = "request.currency"),
            @Mapping(target = "merchant", source = "request.merchant"),
            @Mapping(target = "externalRef", source = "request.externalRef"),
            @Mapping(target = "notes", source = "request.notes"),
            @Mapping(target = "occurredAt", expression = "java(toInstant(request.getOccurredAt()))"),
            @Mapping(target = "userId", source = "userId"),
            @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())"),
            @Mapping(target = "lastAction", constant = "UPDATED")
    })
    void updateNonStructuralFields(@MappingTarget Transaction transaction,
            UpdateTransactionRequest request,
            String userId);

    // ---------- ENTITY TO DTO ----------

    @Mapping(target = "occurredAt", expression = "java(mapInstantToLocalDateTime(transaction.getOccurredAt()))")
    @Mapping(target = "postedAt", expression = "java(mapInstantToLocalDateTime(transaction.getPostedAt()))")
    @Mapping(target = "amount", expression = "java(mapBigDecimalToDouble(transaction.getAmount()))")
    TransactionDto toDto(Transaction transaction);

    // ---------- ENTITY TO RESPONSE ----------

    @Mapping(target = "occurredAt", expression = "java(mapInstantToLocalDateTime(transaction.getOccurredAt()))")
    @Mapping(target = "postedAt", expression = "java(mapInstantToLocalDateTime(transaction.getPostedAt()))")
    @Mapping(target = "amount", expression = "java(mapBigDecimalToDouble(transaction.getAmount()))")
    TransactionDto toResponse(Transaction transaction);

    @Mapping(target = "occurredAt", expression = "java(mapInstantToLocalDateTime(entity.getOccurredAt()))")
    @Mapping(target = "postedAt", expression = "java(mapInstantToLocalDateTime(entity.getPostedAt()))")
    TransactionWithCategoryAndAccountDto toDto(TransactionsWithCategoryAndAccount entity);

    TransactionsAverageDto toDto(TransactionsAverage entity);

    // ---------- DTO TO Request ----------

    CreateTransactionRequest toRequest(CreateTransactionRequestDto dto);
    UpdateTransactionRequest toRequest(UpdateTransactionRequestDto dto);
    // ---------- HELPER MAPPINGS ----------

    default TransactionType mapType(String type) {
        return TransactionType.fromValueIgnoreCase(type);
    }

    default Instant toInstant(String value) {
        if (value == null)
            return null;

        try {
            return Instant.parse(value); // ISO instant
        } catch (DateTimeParseException ignored) {
        }

        // fallback: treat as local datetime in APP_ZONE_ID
        LocalDateTime ldt = LocalDateTime.parse(value, LOCAL_DATE_TIME_FORMATTER);
        return ldt.atZone(APP_ZONE_ID).toInstant();
    }

    default String fromInstant(Instant instant) {
        return instant == null ? null : instant.toString();
    }

    default LocalDateTime mapInstantToLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, APP_ZONE_ID);
    }

    default Double mapBigDecimalToDouble(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.doubleValue();
    }
}
