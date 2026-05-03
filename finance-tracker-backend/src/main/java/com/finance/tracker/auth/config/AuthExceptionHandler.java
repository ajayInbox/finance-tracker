package com.finance.tracker.auth.config;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class AuthExceptionHandler {

    @Builder
    @Getter
    public static class ValidationErrorResponse {
        private Integer statusCode;
        private String message;
        private Map<String, String> errors; // field -> message
    }

    @Builder
    @Getter
    public static class ErrorResponse {
        private Integer statusCode;
        private String message;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("Validation failed: {}", errors);

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .errors(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {

        log.error("Unexpected error", ex);

        ErrorResponse response = ErrorResponse.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Something went wrong")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {

        ErrorResponse response = ErrorResponse.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.badRequest().body(response);
    }
}
