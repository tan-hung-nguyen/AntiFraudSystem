package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class UsernameReservedWordException extends RegisterException {
    public UsernameReservedWordException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
