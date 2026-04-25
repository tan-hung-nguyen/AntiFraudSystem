package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;
public class UserActiveStatusException extends AuthServiceException {
    public UserActiveStatusException(String message, HttpStatus status) {
        super(message,status);
    }
}
