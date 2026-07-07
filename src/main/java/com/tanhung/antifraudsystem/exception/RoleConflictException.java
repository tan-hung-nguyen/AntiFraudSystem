package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class RoleConflictException extends RoleChangeException {
    public RoleConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
