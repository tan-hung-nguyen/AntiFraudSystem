package com.tanhung.antifraudsystem.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public class AntiFraudServiceException extends RuntimeException {
    private final HttpStatus status;
    public AntiFraudServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
