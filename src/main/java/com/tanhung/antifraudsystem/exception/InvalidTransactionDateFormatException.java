package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class InvalidTransactionDateFormatException extends InvalidTransactionDataException {
    public InvalidTransactionDateFormatException(String message, HttpStatus status) {
        super(message, status);
    }
}
