package com.finance.tracker.transactions.service;

import com.finance.tracker.transactions.domain.SmsMessage;

import java.util.List;

public interface TransactionSmsService {
    void exportMessages(List<SmsMessage> messageList);
    void exportMessagesSendToQueue(List<SmsMessage> messageList);
    void createTransactionFromQueueMsg(SmsMessage message);
}
