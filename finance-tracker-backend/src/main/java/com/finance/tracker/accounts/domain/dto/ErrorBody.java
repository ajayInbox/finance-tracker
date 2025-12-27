package com.finance.tracker.accounts.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorBody {

    private int errorStatusCode;
    private String errorMessage;

}
