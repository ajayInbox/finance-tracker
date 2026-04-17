package com.finance.tracker.transactions.service;

import com.finance.tracker.transactions.domain.SmsRequest;
import com.finance.tracker.transactions.domain.entities.UnparsedSmsLog;
import com.finance.tracker.transactions.repository.UnparsedSmsLogsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionAuditService {
    private final UnparsedSmsLogsRepository logsRepository;

    @Async
    public void logFailedParsing(SmsRequest msg, String reason) {
        UnparsedSmsLog logEntry = new UnparsedSmsLog();
        logEntry.setSender(msg.getSender());
        logEntry.setSmsRawBody(msg.getBody());
        logEntry.setTimestamp(msg.getTimestamp());
        logEntry.setErrorReason(reason);
        logsRepository.save(logEntry);
    }
}