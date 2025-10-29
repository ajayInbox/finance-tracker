package com.finance.tracker.transactions.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class TransactionCreateUpdateRequest {

        private Double amount;
        private String type;
        private String merchant;
        private String notes;
        private String transactionName;

        // account for example: bank; credit; loan acc etc.
        //TODO need to create seperate entity for account
        private String account;

        // categories for example: Shopping; food; Housing; Income
        //TODO need to create seperate entity for category
        private String category;
        private List<String>tags;
        // ISO DATE TIME FORMAT :: YYYY-MM-DDThh:mm:ss
        private String occuredAt;
        // ISO DATE TIME FORMAT :: YYYY-MM-DDThh:mm:ss
        private String postedAt;
        private String currency;

        // attachments can be images
        //TODO need to create seperate entity for attachment
        private String attachments;

        // source of data like added from form or added through sms notification etc.
        //TODO need to add seperate entity for external ref
        private String externalRef;

}
