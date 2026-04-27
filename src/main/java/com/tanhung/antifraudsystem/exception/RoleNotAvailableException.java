package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class RoleNotAvailableException extends RoleChangeException {
    public RoleNotAvailableException(String message, HttpStatus status) {
        super(message, status);
    }
}
