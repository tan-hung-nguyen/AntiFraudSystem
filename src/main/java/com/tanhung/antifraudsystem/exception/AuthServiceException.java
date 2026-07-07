package com.tanhung.antifraudsystem.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthServiceException extends RuntimeException {
    private final HttpStatus status;
    protected AuthServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
