package com.tanhung.antifraudsystem.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public class ActivationException extends AuthServiceException {
    public ActivationException(String message, HttpStatus status) {
        super(message,status);
    }
}
