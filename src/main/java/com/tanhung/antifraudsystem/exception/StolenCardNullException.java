package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class StolenCardNullException extends StolenCardException {
    public StolenCardNullException(String message, HttpStatus status) {
        super(message, status);
    }
}
