package com.finance.tracker.accounts.controller;

import com.finance.tracker.accounts.domain.dto.ErrorBody;
import com.finance.tracker.accounts.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;

@RestControllerAdvice
@Slf4j
public class AccountErrorControllerAdvice {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorBody> handleAccountNotFoundException(AccountNotFoundException ex){
        log.error("caught AccountNotFoundException exception");
        ErrorBody errorBody = ErrorBody.builder()
                .errorStatusCode(HttpStatus.NOT_FOUND.value())
                .errorMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorBody, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateLastFourException.class)
    public ResponseEntity<ErrorBody> handleDuplicateLastFourException(DuplicateLastFourException ex){
        log.error("caught DuplicateLastFourException exception");
        ErrorBody errorBody = ErrorBody.builder()
                .errorStatusCode(HttpStatus.BAD_REQUEST.value())
                .errorMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AmountGtCurrentBalance.class)
    public ResponseEntity<ErrorBody> handleAmountGtCurrentBalance(AmountGtCurrentBalance ex){
        log.error("caught AmountGtCurrentBalance exception");
        ErrorBody errorBody = ErrorBody.builder()
                .errorStatusCode(HttpStatus.BAD_REQUEST.value())
                .errorMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccountAmountNegativeException.class)
    public ResponseEntity<ErrorBody> handleAccountAmountNegativeException(AccountAmountNegativeException ex){
        log.error("caught AccountAmountNegativeException exception");
        ErrorBody errorBody = ErrorBody.builder()
                .errorStatusCode(HttpStatus.BAD_REQUEST.value())
                .errorMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CurrentOutstandingGtCreditLimit.class)
    public ResponseEntity<ErrorBody> handleCurrentOutstandingGtCreditLimit(CurrentOutstandingGtCreditLimit ex){
        log.error("caught CurrentOutstandingGtCreditLimit exception");
        ErrorBody errorBody = ErrorBody.builder()
                .errorStatusCode(HttpStatus.BAD_REQUEST.value())
                .errorMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorBody> handleHttpServerErrorException(Exception ex){
        log.error("caught Unexpected exception");
        ErrorBody errorBody = ErrorBody.builder()
                .errorStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
