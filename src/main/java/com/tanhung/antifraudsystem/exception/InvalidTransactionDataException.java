package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class InvalidTransactionDataException extends AntiFraudServiceException {
    public InvalidTransactionDataException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
