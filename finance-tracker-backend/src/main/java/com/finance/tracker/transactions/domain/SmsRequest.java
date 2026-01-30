package com.finance.tracker.transactions.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsRequest {

    private String uniqueIdentifier;
    private String sender;
    private String body;
    private String timestamp;

}
