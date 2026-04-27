package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class StolenCardNumberNotFoundException extends StolenCardException {
    public StolenCardNumberNotFoundException(String message, HttpStatus status) {
        super(message, status);
    }
}
