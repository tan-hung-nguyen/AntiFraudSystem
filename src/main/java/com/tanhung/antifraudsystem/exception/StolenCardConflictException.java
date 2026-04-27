package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class StolenCardConflictException extends StolenCardException {
    public StolenCardConflictException(String message, HttpStatus status) {
        super(message, status);
    }
}
