package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class PhoneNumberConflictException extends RegisterException {
    public PhoneNumberConflictException(String message, HttpStatus status) {
        super(message, status);
    }
}
