package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class RoleConflictException extends RoleChangeException {
    public RoleConflictException(String message, HttpStatus status) {
        super(message, status);
    }
}
