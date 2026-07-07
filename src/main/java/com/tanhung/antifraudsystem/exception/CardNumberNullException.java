package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class StolenCardNumberNullException extends StolenCardException {
    public StolenCardNumberNullException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
