package com.finance.tracker.transactions.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SmsMessageList {

    List<SmsMessage> smsMessages;

}
