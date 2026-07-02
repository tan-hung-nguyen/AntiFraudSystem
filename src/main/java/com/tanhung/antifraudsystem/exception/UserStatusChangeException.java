package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;
public class UserStatusChangeException extends AuthServiceException {
    public UserStatusChangeException(String message, HttpStatus status) {
        super(message,status);
    }
}
