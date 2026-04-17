package com.finance.tracker.transactions.controller;

import com.finance.tracker.transactions.domain.dtos.ErrorDto;
import com.finance.tracker.sync.exceptions.InvalidScanStateException;
import com.finance.tracker.sync.exceptions.InvalidSyncRequestException;
import com.finance.tracker.sync.exceptions.ScanAccessDeniedException;
import com.finance.tracker.sync.exceptions.ScanNotFoundException;
import com.finance.tracker.transactions.exceptions.TransactionNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ErrorController {

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorDto> handleTransactionNotFoundException(TransactionNotFoundException ex){
        log.error("caught TransactionNotFoundException exception");
        ErrorDto errorDto = ErrorDto.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .errorMessage("Transaction not found")
                .build();
        return new ResponseEntity<>(errorDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ScanNotFoundException.class)
    public ResponseEntity<ErrorDto> handleScanNotFoundException(ScanNotFoundException ex) {
        log.error("caught ScanNotFoundException exception", ex);
        ErrorDto errorDto = ErrorDto.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .errorMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ScanAccessDeniedException.class)
    public ResponseEntity<ErrorDto> handleScanAccessDeniedException(ScanAccessDeniedException ex) {
        log.error("caught ScanAccessDeniedException exception", ex);
        ErrorDto errorDto = ErrorDto.builder()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .errorMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorDto, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({InvalidSyncRequestException.class, InvalidScanStateException.class})
    public ResponseEntity<ErrorDto> handleInvalidSyncRequest(RuntimeException ex) {
        log.error("caught sync validation exception", ex);
        HttpStatus status = ex instanceof InvalidScanStateException ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        ErrorDto errorDto = ErrorDto.builder()
                .statusCode(status.value())
                .errorMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorDto, status);
    }

}
