package com.finance.tracker.transactions.domain.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorDto {

    private Integer statusCode;
    private String errorMessage;

}

