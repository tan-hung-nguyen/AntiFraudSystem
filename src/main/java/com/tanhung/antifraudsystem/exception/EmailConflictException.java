package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class EmailConflictException extends RegisterException {
    public EmailConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
