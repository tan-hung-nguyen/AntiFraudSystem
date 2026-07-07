package com.tanhung.antifraudsystem.exception;

import org.springframework.http.HttpStatus;

public class InvalidOperationChangeException extends UserStatusChangeException {
    public InvalidOperationChangeException(String message) {
        super(message);
    }
}
