package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class InvalidCardNumberException extends StolenCardException {
    public InvalidCardNumberException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
