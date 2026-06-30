package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class UsernameConflictException extends RegisterException {
    public UsernameConflictException(String message, HttpStatus status) {
        super(message, status);
    }
}
