package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;
public class StolenCardException extends AntiFraudServiceException {
    public StolenCardException(String message, HttpStatus status) {
        super(message, status);
    }
}

