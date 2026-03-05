package com.finance.tracker.transactions.domain;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ValidationRequest {

    private UUID userId;
    private UUID accountId;
    private UUID categoryId;
    private String type;
    private Currency currency;

}
