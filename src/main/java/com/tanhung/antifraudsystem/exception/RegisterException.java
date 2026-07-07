package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class RegisterException extends AuthServiceException {
    protected RegisterException(String message, HttpStatus status) {
        super(message, status);
    }
}
