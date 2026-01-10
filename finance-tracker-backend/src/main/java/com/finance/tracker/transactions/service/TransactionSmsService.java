package com.finance.tracker.transactions.service;

import com.finance.tracker.transactions.domain.ParsedTransaction;
import com.finance.tracker.transactions.domain.SmsMessage;

import java.util.List;
import java.util.Optional;

public interface TransactionSmsService {
    void exportMessages(List<SmsMessage> messageList);
    void exportMessagesSendToQueue(List<SmsMessage> messageList);
    Optional<ParsedTransaction> parseTransactionFromSms(SmsMessage message);
}
