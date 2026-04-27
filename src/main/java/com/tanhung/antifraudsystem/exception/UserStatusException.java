package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;
public class UserStatusException extends AuthServiceException {
    public UserStatusException(String message, HttpStatus status) {
        super(message,status);
    }
}
