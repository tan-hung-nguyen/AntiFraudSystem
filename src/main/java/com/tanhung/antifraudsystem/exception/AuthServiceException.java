package com.tanhung.antifraudsystem.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthServiceException extends RuntimeException {
    private final HttpStatus status;
    public AuthServiceException(String message, HttpStatus status) {
        this.status = status;
        super(message);
    }
}
