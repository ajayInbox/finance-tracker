package com.finance.tracker.transactions.service;

import com.finance.tracker.transactions.domain.ParsedTransaction;
import com.finance.tracker.transactions.domain.SmsRequest;

import java.util.Optional;

public interface SmsParserService {

    Optional<ParsedTransaction> parse(SmsRequest sms);

}
