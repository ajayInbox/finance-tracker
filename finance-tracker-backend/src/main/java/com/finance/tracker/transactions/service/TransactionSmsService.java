package com.finance.tracker.transactions.service;

import com.finance.tracker.transactions.domain.ParsedTransaction;
import com.finance.tracker.transactions.domain.SmsRequest;

import java.util.List;
import java.util.Optional;

public interface TransactionSmsService {
    void exportMessages(List<SmsRequest> messageList);
    void exportMessagesSendToQueue(List<SmsRequest> messageList);
    Optional<ParsedTransaction> parseTransactionFromSms(SmsRequest message);
}
