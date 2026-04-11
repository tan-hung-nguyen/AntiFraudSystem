package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class RegistrationException extends AuthServiceException {
    public RegistrationException(String message, HttpStatus status) {
        super(message, status);
    }
}
