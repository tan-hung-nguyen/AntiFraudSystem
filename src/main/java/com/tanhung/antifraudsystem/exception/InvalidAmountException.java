package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;
public class InvalidAmountException extends AntiFraudServiceException {
    public InvalidAmountException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
