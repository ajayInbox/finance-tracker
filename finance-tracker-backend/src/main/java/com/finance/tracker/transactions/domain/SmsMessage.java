package com.finance.tracker.transactions.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsMessage {

    private String messageAddress;
    private String messageHeader;
    private String messageBody;
    private String messageDate;

}
