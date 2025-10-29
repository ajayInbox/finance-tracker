package com.finance.tracker.transactions.mapper;

import com.finance.tracker.transactions.domain.TransactionCreateUpdateRequest;
import com.finance.tracker.transactions.domain.TransactionsAverage;
import com.finance.tracker.transactions.domain.TransactionsWithCategoryAndAccount;
import com.finance.tracker.transactions.domain.dtos.TransactionCreateUpdateRequestDto;
import com.finance.tracker.transactions.domain.dtos.TransactionDto;
import com.finance.tracker.transactions.domain.dtos.TransactionWithCategoryAndAccountDto;
import com.finance.tracker.transactions.domain.dtos.TransactionsAverageDto;
import com.finance.tracker.transactions.domain.entities.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionCreateUpdateRequest toTransactionCreateUpdateRequest(TransactionCreateUpdateRequestDto dto);

    TransactionDto toDto(Transaction transaction);

    TransactionWithCategoryAndAccountDto toDto(TransactionsWithCategoryAndAccount transactionsWithCategoryAndAccount);

    TransactionsAverageDto toDto(TransactionsAverage average);

}
