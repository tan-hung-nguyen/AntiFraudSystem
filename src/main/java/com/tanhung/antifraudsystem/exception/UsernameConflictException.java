package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class RegisterConflictException extends RegisterException {
    public RegisterConflictException(String message, HttpStatus status) {
        super(message, status);
    }
}
