package com.finance.tracker.transactions.domain;

import java.util.List;

import static io.github.validcheck.Check.batch;

public record TransactionCreateUpdateRequest(
        Double amount,
        String type,
        String merchant,
        String notes,
        String transactionName,

        // account for example: bank, credit, loan acc etc.
        //TODO need to create seperate entity for account
        String account,

        // categories for example: Shopping, food, Housing, Income
        //TODO need to create seperate entity for category
        String category,
        List<String>tags,
        // ISO DATE TIME FORMAT :: YYYY-MM-DDThh:mm:ss
        String occuredAt,
        // ISO DATE TIME FORMAT :: YYYY-MM-DDThh:mm:ss
        String postedAt,
        String currency,

        // attachments can be images
        //TODO need to create seperate entity for attachment
        String attachments,

        // source of data like added from form or added through sms notification etc.
        //TODO need to add seperate entity for external ref
        String externalRef

) 
{
    public TransactionCreateUpdateRequest {
        var validation = batch();
        validation.check(amount, "amount").notNull().between(1d, 10000d);
        validation.check(type, "type").notNull().oneOf("INCOME","EXPENSE","Income","Expense");
//        validation.check(account, "account").notNull();
//        validation.check(category, "category").notNull();
        validation.validate();
    }
}
