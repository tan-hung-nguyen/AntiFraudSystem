package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class UsernameReservedWordException extends RegisterException {
    public UsernameReservedWordException(String message, HttpStatus status) {
        super(message, status);
    }
}
