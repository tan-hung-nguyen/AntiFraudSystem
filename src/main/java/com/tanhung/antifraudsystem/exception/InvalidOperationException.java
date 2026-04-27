package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class InvalidOperationException extends UserStatusException {
    public InvalidOperationException(String message, HttpStatus status) {
        super(message, status);
    }
}
