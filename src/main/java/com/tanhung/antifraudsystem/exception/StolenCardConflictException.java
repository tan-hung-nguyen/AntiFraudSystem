package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class StolenCardConflictException extends StolenCardException {
    public StolenCardConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
