package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class RegisterNullException extends RegisterException {
    public RegisterNullException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
