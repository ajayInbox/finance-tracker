package com.finance.tracker.category.controller;

import com.finance.tracker.category.exceptions.CategoryNotFoundException;
import com.finance.tracker.transactions.domain.dtos.ErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class CategoryErrorController {

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCategoryNotFoundException(CategoryNotFoundException ex){
        log.error("caught TransactionNotFoundException exception");
        ErrorDto errorDto = ErrorDto.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .errorMessage("Transaction not found")
                .build();
        return new ResponseEntity<>(errorDto, HttpStatus.NOT_FOUND);
    }
}
