package com.tanhung.antifraudsystem.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public class ActivationException extends RuntimeException {
    private final HttpStatus status;
    public ActivationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
