package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class RoleNotAvailableException extends RoleChangeException {
    public RoleNotAvailableException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
