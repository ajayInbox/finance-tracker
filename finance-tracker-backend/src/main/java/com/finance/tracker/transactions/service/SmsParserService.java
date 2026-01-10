package com.finance.tracker.transactions.service;

import com.finance.tracker.transactions.domain.ParsedTransaction;
import com.finance.tracker.transactions.domain.SmsMessage;

import java.util.Optional;

public interface SmsParserService {

    Optional<ParsedTransaction> parse(SmsMessage sms);

}
