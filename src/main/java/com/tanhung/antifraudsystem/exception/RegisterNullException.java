package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class RegisterNullException extends RegisterException {
    public RegisterNullException(String message, HttpStatus status) {
        super(message, status);
    }
}
